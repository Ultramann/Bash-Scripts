#!/bin/bash

while getopts "mck" OPTION
do
    case $OPTION in
        m)
            echo "Spinning up spark cluster..."
            # Create tmux session for master node, start master node, and detach from session
            tmux new -d -s master '\
            ${SPARK_HOME}/bin/spark-class org.apache.spark.deploy.master.Master \
                -h 127.0.0.1 \
                -p 7077 \
                --webui-port 8080
            '
            # Create tmux session for first worker node, start node, and detach from session
            tmux new -d -s worker1 '\
            ${SPARK_HOME}/bin/spark-class org.apache.spark.deploy.worker.Worker \
                -c 1 \
                -m 1G \
                spark://127.0.0.1:7077
            '
            # Create tmux session for second worker node, start node, and detach from session
            tmux new -d -s worker2 '\
            ${SPARK_HOME}/bin/spark-class org.apache.spark.deploy.worker.Worker \
                -c 1 \
                -m 1G \
                spark://127.0.0.1:7077
            '
            # Start tmux session for ipython notebook server, start server on the master node,
                # detach from session
            tmux new -d -s ipython '\
            IPYTHON_OPTS="notebook"  ${SPARK_HOME}/bin/pyspark \
                --master spark://127.0.0.1:7077 \
                --executor-memory 1G \
                --driver-memory 1G
            '
            echo "Done"
            exit
            ;;
        c)
            if tmux info &> /dev/null; 
            then 
                echo "Spark cluster:"
                tmux ls
            else
              echo "No cluster running"
            fi
            exit
            ;;
        k)
            echo "Shutting down spark cluster..."
            # Kill all sessions but the most recent one
            tmux kill-session -a
            # Kill the remaining session
            tmux kill-session
            echo "Done"
            exit
            ;;
        \?)
            echo "\t Use -m to make spark cluster with two workers and start ipython notebook"
            echo "\t Use -c to check for existance of a cluster"
            echo "\t Use -k to kill all tmux sessions (the cluster)"
            echo "\t\t Requires tmux"
            exit
            ;;
    esac
done
