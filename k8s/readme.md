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

Start a pod.

```bash
kubectl apply -f pod.yaml
```

Attach to it.

```bash
winpty kubectl attach -it pods/myspark
```

## State
1) Launching spark-submit with k8s master.

## Problems current  

### 1)
Some kind of permissions problem when launching spark-submit.

```bash
ERROR Client: Please check "kubectl auth can-i create pod" first. It should be yes.
Exception in thread "main" io.fabric8.kubernetes.client.KubernetesClientException: Operation: [create]  for kind: [Pod]  with name: [nu
ll]  in namespace: [default]  failed.
        at io.fabric8.kubernetes.client.KubernetesClientException.launderThrowable(KubernetesClientException.java:64)
        at io.fabric8.kubernetes.client.KubernetesClientException.launderThrowable(KubernetesClientException.java:72)
        at io.fabric8.kubernetes.client.dsl.base.BaseOperation.create(BaseOperation.java:380)
        at io.fabric8.kubernetes.client.dsl.base.BaseOperation.create(BaseOperation.java:86)
        at org.apache.spark.deploy.k8s.submit.Client.run(KubernetesClientApplication.scala:141)
        at org.apache.spark.deploy.k8s.submit.KubernetesClientApplication.$anonfun$run$4(KubernetesClientApplication.scala:220)
        at org.apache.spark.deploy.k8s.submit.KubernetesClientApplication.$anonfun$run$4$adapted(KubernetesClientApplication.scala:214)
        at org.apache.spark.util.Utils$.tryWithResource(Utils.scala:2713)
        ...
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

