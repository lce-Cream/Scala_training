## Workflow

build app's docker image  
start docker daemon

```shell
minikube start driver=hyperv
minikube image load arseni/app

kubectl apply -f secret.yaml
kubectl apply -f deployment.yaml

kubectl get deployments
kubectl get pods -o wide

kubectl describe pod myapp-deployment-68876d9b75-dthhw
kubectl logs myapp-deployment-68876d9b75-dthhw

kubectl get secret
kubectl get all
kubectl get pod --watch
kubectl attach -ti pod/app-deployment-6fffdccd68-bcnt6

kubectl delete deployment app-deployment
```

## Current state & Problems  
  
All previous problems were solved by adding tty and stdin keys in deployment.yaml.

```yaml
containers:
  - name: app-container
    image: arseni/app
    tty: true
    stdin: true
```
