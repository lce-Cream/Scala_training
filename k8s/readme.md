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

kubectl delete deployment app-deployment
```

## Current state & Problems  
  
Now pod sustains itself and doesn't crush anymore.

```shell
PS E:\other\Scala practice\ScalaTraining> kubectl get all
NAME                                  READY   STATUS    RESTARTS   AGE
pod/app-deployment-67c69fb774-sh2l4   1/1     Running   0          6m25s

NAME                             READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/app-deployment   1/1     1            1           6m26s
```

But when I try to view its logs or attach to it, I get this. Seems like scala.io.StdIn.readLine("$ ") just can't
get a control over process execution. This behaviour is only shown in kubernetes pod, both sbt run and docker container
start work as intended.

```text
 $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $
 $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $
 $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $
 $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $
 $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ $ ... 
```
