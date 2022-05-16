## Workflow  

### Running a job  

Start docker daemon. Build app's docker image.

```bash
docker build -f ./Dockerfile --cache-from bitnami/spark -t arseni/app .
```

Start minikube.

```bash
minikube start driver=hyperv; minikube status
```

Load this image in minikube cluster.

```bash
minikube image load arseni/app
```

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

```yaml
args: ["/bin/sh","-c","spark-submit --jars /application/lib/*,/application/* --class Main /application/test_4.jar -m db2 -a read -n 5"]
```

Start the job.

```bash
kubectl apply -f job.yaml
```

Check the result.

```bash
kubectl get pods

NAME               READY   STATUS      RESTARTS   AGE
pod/reader-tmjzd   0/1     Completed   0          19s
```

```bash
kubectl logs pod/reader-tmjzd

 13:09:07.45
 13:09:07.46 Welcome to the Bitnami spark container
 13:09:07.46 Subscribe to project updates by watching https://github.com/bitnami/bitnami-docker-spark
 13:09:07.46 Submit issues and feature requests at https://github.com/bitnami/bitnami-docker-spark/issues
 13:09:07.47

22/05/14 13:09:32 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
22/05/14 13:09:41 INFO DB2Connection: Reading table: ARSENI_SALES_DATA
+----------+-------------+----+------+
|product_id|product_group|year| sales|
+----------+-------------+----+------+
|         0|            0|2015|787756|
|         8|            1|2015|586892|
|         1|            4|2015|574870|
|         3|            6|2015|627011|
|        10|            3|2016|602343|
+----------+-------------+----+------+
```

### Spark on Kubernetes

Start docker daemon, start minikube.

```bash
minikube start --driver=hyperv
```

Build Spark image in minikube docker daemon.

```bash
docker-image-tool.sh -m -t arseni build
```

## State
1) Making Spark k8s cluster.

## Problems current  

### 1)
Trying to connect to a spark container in minikube.
Feels like I don't actually understand how it works. Am I supposed to run spark image in a pod and then
use spark-submit outside the k8s or do it inside the pod? Maybe I don't even need to build this spark image and
rather just use my previous one with some tweaks. Anyways this image seems not working.

```bash
PS C:\Users\user\Desktop\Scala_training> kubectl run myspark --image=spark:arseni
PS C:\Users\user\Desktop\Scala_training> kubectl logs pod/myspark
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
+ '[' -z ']'
+ '[' -z x ']'
+ SPARK_CLASSPATH='/opt/spark/conf::/opt/spark/jars/*'
+ case "$1" in
+ echo 'Non-spark-on-k8s command provided, proceeding in pass-through mode...'
Non-spark-on-k8s command provided, proceeding in pass-through mode...
+ CMD=("$@")
+ exec /usr/bin/tini -s --
tini (tini version 0.19.0)
Usage: tini [OPTIONS] PROGRAM -- [ARGS] | --version

Execute a program under the supervision of a valid init process (tini)

Command line options:

  --version: Show version and exit.
  -h: Show this help message and exit.
  -s: Register as a process subreaper (requires Linux >= 3.4).
  -p SIGNAL: Trigger SIGNAL when parent dies, e.g. "-p SIGKILL".
  -v: Generate more verbose output. Repeat up to 3 times.
  -w: Print a warning when processes are getting reaped.
  -g: Send signals to the child's process group.
  -e EXIT_CODE: Remap EXIT_CODE (from 0 to 255) to 0.
  -l: Show license and exit.

Environment variables:

  TINI_SUBREAPER: Register as a process subreaper (requires Linux >= 3.4).
  TINI_VERBOSITY: Set the verbosity level (default: 1).
  TINI_KILL_PROCESS_GROUP: Send signals to the child's process group.
```

## Problems solved  
  
All previous problems with CLI malfunctioning were solved by adding tty and stdin keys in deployment.yaml.

```yaml
containers:
  - name: app-container
    image: arseni/app
    tty: true
    stdin: true
```

---

When trying to build spark image I stumbled upon this error.

```bash
$ ./docker-image-tool.sh -t arseni build
ls: cannot access 'E:\software\Spark\spark-3.1.3-3.2/jars/spark-*': No such file or directory
Cannot find Spark JARs. This script assumes that Apache Spark has first been built locally or this is a runnable distribution.

```

Then I looked up in this docker-image-tool.sh script and found next lines.
```bash
local TOTAL_JARS=$(ls $SPARK_ROOT/jars/spark-* | wc -l)
TOTAL_JARS=$(( $TOTAL_JARS ))
if [ "${TOTAL_JARS}" -eq 0 ]; then
  error "Cannot find Spark JARs. This script assumes that Apache Spark has first been built locally or this is a runnable distribution."
fi
```

Windows SPARK_ROOT path variable holds Windows specific path with '\\' delimeter signs and that being substituted causes
invalid mixture of '\\' and '/' signs in path. So I just hard coded my path and it worked out.

```bash
local TOTAL_JARS=$(ls E:/software/Spark/spark-3.1.3-3.2/jars/spark-* | wc -l)
```

Probably I should start using Linux at least on a virtual machine to avoid such inconveniences.

---

When starting spark-submit being attached to the pod there was an error.

```bash
PS E:\other\Scala practice\ScalaTraining> kubectl attach -it app-deployment-6fffdccd68-868ck
If you don't see a command prompt, try pressing enter.

$ spark-submit --jars /application/lib/* --class Main /application/test_4.jar --mode cos --action read --number 5
22/05/12 21:25:48 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Exception in thread "main" org.apache.spark.SparkException: No main class set in JAR; please specify one with --class.
        at org.apache.spark.deploy.SparkSubmit.error(SparkSubmit.scala:972)
        at org.apache.spark.deploy.SparkSubmit.prepareSubmitEnvironment(SparkSubmit.scala:492)
        at org.apache.spark.deploy.SparkSubmit.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:898)
        at org.apache.spark.deploy.SparkSubmit.doRunMain$1(SparkSubmit.scala:180)
        at org.apache.spark.deploy.SparkSubmit.submit(SparkSubmit.scala:203)
        at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:90)
        at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:1043)
        at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:1052)
        at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
```

Fixed by adding /application/test_4.jar in --jars argument.

```bash
spark-submit --class Main --jars /application/lib/*,/application/test_4.jar /application/test_4.jar -m cos -a read -n 5
```

---

When I try to launch my job.yaml in kubernetes.

```bash
PS E:\other\Scala practice\ScalaTraining\k8s> kubectl delete -f .\job.yaml; kubectl apply -f .\job.yaml; kubectl get all
job.batch "reader" deleted
job.batch/reader created
NAME                                  READY   STATUS              RESTARTS      AGE
pod/app-deployment-6fffdccd68-868ck   1/1     Running             2 (17h ago)   17h
pod/reader-jrnp5                      0/1     ContainerCreating   0             0s
...
NAME               COMPLETIONS   DURATION   AGE
job.batch/reader   0/1           0s         0s

PS E:\other\Scala practice\ScalaTraining\k8s> kubectl logs reader-jrnp5
Exception in thread "main" java.lang.IllegalArgumentException: basedir must be absolute: ?/.ivy2/local
        at org.apache.ivy.util.Checks.checkAbsolute(Checks.java:48)
        at org.apache.ivy.plugins.repository.file.FileRepository.setBaseDir(FileRepository.java:131)
        at org.apache.ivy.plugins.repository.file.FileRepository.<init>(FileRepository.java:44)
        at org.apache.spark.deploy.SparkSubmitUtils$.createRepoResolvers(SparkSubmit.scala:1179)
        at org.apache.spark.deploy.SparkSubmitUtils$.buildIvySettings(SparkSubmit.scala:1281)
        at org.apache.spark.util.DependencyUtils$.resolveMavenDependencies(DependencyUtils.scala:182)
        at org.apache.spark.deploy.SparkSubmit.prepareSubmitEnvironment(SparkSubmit.scala:308)
        at org.apache.spark.deploy.SparkSubmit.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:898)
        at org.apache.spark.deploy.SparkSubmit.doRunMain$1(SparkSubmit.scala:180)
        at org.apache.spark.deploy.SparkSubmit.submit(SparkSubmit.scala:203)
        at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:90)
        at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:1043)
        at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:1052)
        at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
```

After some stackoverflow https://stackoverflow.com/questions/50861477/basedir-must-be-absolute-ivy2-local
I appended `--conf spark.jars.ivy=/tmp/.ivy` to my spark-submit and got this

```bash
PS E:\other\Scala practice\ScalaTraining\k8s> kubectl logs reader-wnslr
22/05/13 15:01:32 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Exception in thread "main" org.apache.hadoop.security.KerberosAuthException: failure to login: javax.security.auth.login.LoginException: java.lang.NullPointerException: invalid null input: name
        at com.sun.security.auth.UnixPrincipal.<init>(UnixPrincipal.java:71)
        at com.sun.security.auth.module.UnixLoginModule.login(UnixLoginModule.java:133)
        ...
        at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
Caused by: javax.security.auth.login.LoginException: java.lang.NullPointerException: invalid null input: name
        at com.sun.security.auth.UnixPrincipal.<init>(UnixPrincipal.java:71)
        ...
        at org.apache.hadoop.security.UserGroupInformation.doSubjectLogin(UserGroupInformation.java:1975)
        ... 28 more
```

Then I used this https://stackoverflow.com/questions/41864985/hadoop-ioexception-failure-to-login
Bud it didn't help either.
And finally this one helped https://stackoverflow.com/questions/62741285/spark-submit-fails-on-kubernetes-eks-with-invalid-null-input-name

---

No internet access in minikube's pod. Solved by recreating minikube cluster.

```bash
minikube stop && minikube delete && minikube start --driver=hyperv --force
```

---

