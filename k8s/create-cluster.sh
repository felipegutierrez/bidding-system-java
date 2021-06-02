!#/bin/bash

echo
echo "create namespace"
kubectl create namespace bidding-system

echo
echo "create RBAC policy"
kubectl create -f k8s/clusterRole.yaml

echo
echo "create Prometheus configuration and rules files"
kubectl create -f k8s/config-map.yaml

echo
echo "create Prometheus deployment"
kubectl create -f k8s/prometheus-deployment.yaml

# check namespace
echo
echo "kubectl get all --namespace=bidding-system"
kubectl get all --namespace=bidding-system

prometheus_pod_name=`kubectl get pods --namespace=bidding-system --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}'`

echo
echo "exposing Prometheus as a service"
kubectl create -f k8s/prometheus-service.yaml --namespace=bidding-system


sleep 10
echo
echo "forward pod port to localhost"
echo "kubectl port-forward prometheus-pod-name 9090:9090 --namespace=bidding-system"
echo $prometheus_pod_name
kubectl port-forward $prometheus_pod_name 9090:9090 --namespace=bidding-system

echo
echo "Delete all pods and namespace"
echo "kubectl delete all --all --namespace=bidding-system"


echo




