#!/usr/bin/env bash
/Users/michalkoszalka/Downloads/spark-2.3.1-bin-hadoop2.7/bin/spark-submit \
--master k8s://https://192.168.99.100:8443 \
--deploy-mode cluster \
--name spark-pi \
--class SimpleApp \
--conf spark.executor.instances=3 \
--conf spark.kubernetes.container.image=michalkoszalka/spark \
local:///Users/michalkoszalka/Workspace/safety-recognition/spark-processor/target/scala-2.12/spark-processor_2.11-0.1.jar