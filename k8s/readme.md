# Workflow
Build docker image.
```bash
docker build -t arseni/spark-app .
```

Start docker daemon. Start minikube.
```bash
minikube start; minikube status
```

Load image in minikube. You can point your shell to minikube's docker daemon before building it by
using `eval $(minikube -p minikube docker-env)`, in that case you won't need to load it manually.
```bash
minikube image load arseni/spark-app
```

Fill secret.yaml with your credentials. Start the secret.
```bash
kubectl apply -f ./k8s/yaml/secret.yaml
```


## Running a job
Supply your arguments to the application in job.yaml `args` according to this help message.
```text
usage: Configure job to run data load and data transform processes
 -a,--action <str>   Choose to read/write data.
 -h,--help           Show help massage.
 -m,--mode <str>     Launch process using db2/cos/local.
 -n,--number <int>   Amount of records to use.
 -v,--verbose        Print debug info during execution.
```

Example that reads 5 records from COS.
```yaml
args: ["/bin/sh","-c","spark-submit --jars /app/*, --class Main app.jar -m cos -a read -n 5"]
```

Start the job.
```bash
kubectl apply -f ./k8s/yaml/job.yaml
```

Check the result.
```bash
kubectl get pods
```
```text
NAME               READY   STATUS      RESTARTS   AGE
pod/reader-tmjzd   0/1     Completed   0          19s
```
```bash
kubectl logs pod/reader-tmjzd
```

The result is in the ./logs/job.logs file.


## Spark in cluster mode
Get your minikube ip address.
```shell
$K8S_SERVER=kubectl.exe config view --output=jsonpath='{.clusters[].cluster.server}'
```

Launch spark-submit.
```shell
spark-submit.cmd `
--master k8s://$K8S_SERVER `
--deploy-mode cluster `
--class Main `
--conf spark.kubernetes.container.image=arseni/spark-app `
--conf spark.executor.instances=2 `
--conf spark.kubernetes.driver.pod.name=my-spark-driver `
--conf spark.kubernetes.driver.podTemplateFile=./k8s/yaml/driver-pod-template.yaml `
--conf spark.driver.extraClassPath=/app/* `
local:///app/app.jar -m db2 -a read -n 5
```

That's all. All outputs (submit.logs, driver.logs) are provided in ./logs directory.
