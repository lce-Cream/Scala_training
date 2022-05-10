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
  
Pod launches successfully, secret works correctly, but app crushes right after trying to parse cli arguments (allegedly)
before I even connect to it. In regular docker container app works flawlessly.

---

```shell
PS E:\other\ScalaTraining\k8s> kubectl get pod --watch
NAME                             READY   STATUS             RESTARTS       AGE
app-deployment-6c498cbf9-48bzp   0/1     CrashLoopBackOff   5 (109s ago)   6m28s
```

```java
Exception in thread "main" java.lang.NullPointerException
at Main$.$anonfun$new$2(Main.scala:110)
at scala.util.control.Breaks.breakable(Breaks.scala:42)
at Main$.delayedEndpoint$Main$1(Main.scala:109)
at Main$delayedInit$body.apply(Main.scala:10)
at scala.Function0.apply$mcV$sp(Function0.scala:39)
at scala.Function0.apply$mcV$sp$(Function0.scala:39)
at scala.runtime.AbstractFunction0.apply$mcV$sp(AbstractFunction0.scala:17)
at scala.App.$anonfun$main$1$adapted(App.scala:80)
at scala.collection.immutable.List.foreach(List.scala:431)
at scala.App.main(App.scala:80)
at scala.App.main$(App.scala:78)
at Main$.main(Main.scala:10)
at Main.main(Main.scala)
at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
at java.lang.reflect.Method.invoke(Method.java:498)
at org.apache.spark.deploy.JavaMainApplication.start(SparkApplication.scala:52)
at org.apache.spark.deploy.SparkSubmit.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:955)
at org.apache.spark.deploy.SparkSubmit.doRunMain$1(SparkSubmit.scala:180)
at org.apache.spark.deploy.SparkSubmit.submit(SparkSubmit.scala:203)
at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:90)
at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:1043)
at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:1052)
at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
```
