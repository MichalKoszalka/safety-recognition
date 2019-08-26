kubectl create -f rbac-config.yaml
helm init --service-account tiller --history-max 200
helm install --set kafka.brokers='kafka-0.kafka-svc.default.svc.cluster.local:9093\,kafka-1.kafka-svc.default.svc.cluster.local:9093\,kafka-2.kafka-svc.default.svc.cluster.local:9093' --name=kafka-minion kafka-minion/kafka-minion --namespace=monitoring