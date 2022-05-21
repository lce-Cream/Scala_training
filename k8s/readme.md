## Workflow  

Start docker daemon. Start minikube.
```bash
minikube start; minikube status
```

Build Spark image using build.sh script provided in this directory. The script uses your local Spark distribution
to build lightweight Spark image, it's 3 times smaller than bitnami/spark image (600MB vs 1700MB) and builds & works
slightly faster.
Note: be sure you are in k8s directory.
```bash
./build.sh -m -t k8s
```

Help message.

```text
Usage: ./build.sh [options]
Builds and loads the built-in Spark Docker image in minikube.

Options:
  -m          Use minikube's Docker daemon.
  -t          Image tag.
  -h          Print this help message.

Using minikube when building images will do so directly into minikube's Docker daemon.
There is no need to push the images into minikube in that case, they'll be automatically
available when running applications inside the minikube cluster.

Examples:
  - Build image in minikube with tag "testing"
    ./build.sh -m -t testing

  - Build docker image
    ./build.sh -t testing
```


### Running a job  

Fill secret.yaml with your credentials. Start the secret.

```bash
kubectl apply -f secret.yaml
```

Supply your arguments in job.yaml `args`.
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
args: ["/bin/sh","-c","spark-submit --jars /app/*, --class Main app.jar -m db2 -a read -n 5"]
```

Start the job.
```bash
kubectl apply -f job.yaml
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


### Spark in cluster mode

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
--conf spark.kubernetes.container.image=spark:k8s `
--conf spark.executor.instances=2 `
--conf spark.kubernetes.driver.pod.name=my-spark-driver `
--conf spark.kubernetes.driver.podTemplateFile=./driver-pod-template.yaml `
--conf spark.driver.extraClassPath=/app/* `
local:///app/app.jar -m db2 -a read -n 5
```

That's all. All outputs (submit.logs, driver.logs) are provided in ./logs directory.


## State
Everything is done.


## Questions

Why does image built with standard Spark Dockerfile always print this in logs? Is it kinda verbose log info, then why
in such a weird format with '+' signs?

```text
PS C:\Users\user> kubectl.exe logs my-spark-driver
++ id -u
+ myuid=185
++ id -g
+ mygid=0
+ set +e
++ getent passwd 185
+ uidentry=
+ set -e
+ '[' -z '' ']'
+ '[' -w /etc/passwd ']'
+ echo '185:x:185:0:anonymous uid:/opt/spark:/bin/false'
+ SPARK_CLASSPATH=':/opt/spark/jars/*'
+ env
+ grep SPARK_JAVA_OPT_
+ sort -t_ -k4 -n
+ sed 's/[^=]*=\(.*\)/\1/g'
+ readarray -t SPARK_EXECUTOR_JAVA_OPTS
+ '[' -n '' ']'
+ '[' -z ']'
+ '[' -z ']'
+ '[' -n '' ']'
+ '[' -z ']'
+ '[' -z x ']'
+ SPARK_CLASSPATH='/opt/spark/conf::/opt/spark/jars/*'
+ case "$1" in
+ shift 1
+ CMD=("$SPARK_HOME/bin/spark-submit" --conf "spark.driver.bindAddress=$SPARK_DRIVER_BIND_ADDRESS" --deploy-mode client "$@")
+ exec /usr/bin/tini -s -- /opt/spark/bin/spark-submit --conf spark.driver.bindAddress=172.17.0.5 --deploy-mode client --properties-file /opt/spark/conf/spark.properties --class Main local:///app/app.jar -m db2 -a read -n 5
```
