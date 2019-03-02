#!/usr/bin/env bash
/Users/michalkoszalka/Workspace/spark-2.4.0-bin-hadoop2.7/bin/spark-submit \
--master k8s://https://192.168.99.107:8443 \
--deploy-mode cluster \
--name spark-pi \
--class SimpleApp \
--conf spark.executor.instances=1 \
--conf spark.executor.memory=4g \
--conf spark.driver.memory=1g \
--conf spark.kubernetes.container.image=michalkoszalka/spark:0.0.2 \
local:///opt/spark/examples/jars/crimes-processor_2.11-0.1.jar