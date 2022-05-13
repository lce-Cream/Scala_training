## Workflow  

### Pod deployment  

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

### Spark on Kubernetes

Start docker daemon, start minikube.

```bash
minikube start
```

Build Spark image in minikube docker daemon.

```bash
docker-image-tool.sh -m -t arseni build
```

## State  

1) Trying to run kubernetes job.
2) Deploying Spark image on minikube.

## Problems current  

### 1)

After I had moved spark-submit call from Dockerfile's CMD to job's args or typed spark-submit directly into
attached deployment, I got a huge pack of various problems. They include missing classes and libraries,
malfunctioning secrets and right after I dealt with all these now it seems like the pod can't use internet connection.
DB2 and COS functions can't see their hosts, and `curl google.com` gets nothing. All of that makes me think,
that `ENTRYPOINT ["/opt/bitnami/scripts/spark/entrypoint.sh"]` layer in bitnami/spark image, which (as I understand)
took commands from mine Dockerfile's CMD, and `CMD ["/opt/bitnami/scripts/spark/run.sh"]` are really important,
and I should try to use them manually instead of seemingly endless suffering in constant problems' abyss. 


### 2)
Errors during Spark image build. When I build it in local docker daemon everything is ok, but with
'-m' flag in docker-image-tool which is supposed to build the image inside minikube, I get this.

```bash
$ ./docker-image-tool.sh -m -t arseni build
Sending build context to Docker daemon    259MB
Step 1/18 : ARG java_image_tag=11-jre-slim
Step 2/18 : FROM openjdk:${java_image_tag}
11-jre-slim: Pulling from library/openjdk
214ca5fb9032: Pulling fs layer
ebf31789c5c1: Pulling fs layer
8741521b2ba4: Pulling fs layer
61e6176efc30: Pulling fs layer
61e6176efc30: Waiting
8741521b2ba4: Verifying Checksum
8741521b2ba4: Download complete
ebf31789c5c1: Verifying Checksum
ebf31789c5c1: Download complete
61e6176efc30: Verifying Checksum
61e6176efc30: Download complete
214ca5fb9032: Download complete
214ca5fb9032: Pull complete
ebf31789c5c1: Pull complete
8741521b2ba4: Pull complete
61e6176efc30: Pull complete
Digest: sha256:8837dcc4ef68236f534495ca266c0072a0a78fab10b241296c8be47ffe83c06b
Status: Downloaded newer image for openjdk:11-jre-slim
 ---> fe17e42ebc78
Step 3/18 : ARG spark_uid=185
 ---> Running in f40d6334b67b
Removing intermediate container f40d6334b67b
 ---> bc6d60edaf36
Step 4/18 : RUN set -ex &&     sed -i 's/http:\/\/deb.\(.*\)/https:\/\/deb.\1/g' /etc/apt/sources.list &&     apt-get update &&     ln -s /lib /lib64 &&     apt install -y bash tini libc6 libpam-modules krb5-user libnss3 procps &&     mkdir -p /opt/spark &&     mkdir -p /opt/spark/examples &&     mkdir -p /opt/spark/work-dir &&     touch /opt/spark/RELEASE &&     rm /bin/sh &&     ln -sv /bin/bash /bin/sh &&     echo "auth required pam_wheel.so use_uid" >> /etc/pam.d/su &&     chgrp root /etc/passwd && chmod ug+rw /etc/passwd &&     rm -rf /var/cache/apt/*
 ---> Running in 8d5d624d4164
+ sed -i s/http:\/\/deb.\(.*\)/https:\/\/deb.\1/g /etc/apt/sources.list
+ apt-get update
Err:1 https://deb.debian.org/debian bullseye InRelease
  Temporary failure resolving 'deb.debian.org'
Err:2 http://security.debian.org/debian-security bullseye-security InRelease
  Temporary failure resolving 'security.debian.org'
Err:3 https://deb.debian.org/debian bullseye-updates InRelease
  Temporary failure resolving 'deb.debian.org'
Reading package lists...
W: Failed to fetch https://deb.debian.org/debian/dists/bullseye/InRelease  Temporary failure resolving 'deb.debian.org'
W: Failed to fetch http://security.debian.org/debian-security/dists/bullseye-security/InRelease  Temporary failure resolving 'security.debian.org'
W: Failed to fetch https://deb.debian.org/debian/dists/bullseye-updates/InRelease  Temporary failure resolving 'deb.debian.org'
W: Some index files failed to download. They have been ignored, or old ones used instead.
+ ln -s /lib /lib64
+ apt install -y bash tini libc6 libpam-modules krb5-user libnss3 procps

WARNING: apt does not have a stable CLI interface. Use with caution in scripts.

Reading package lists...
Building dependency tree...
Reading state information...
Package krb5-user is not available, but is referred to by another package.
This may mean that the package is missing, has been obsoleted, or
is only available from another source

E: Unable to locate package tini
E: Package 'krb5-user' has no installation candidate
E: Unable to locate package libnss3
E: Unable to locate package procps
The command '/bin/sh -c set -ex &&     sed -i 's/http:\/\/deb.\(.*\)/https:\/\/deb.\1/g' /etc/apt/sources.list &&     apt-get update &&     ln -s /lib /lib64 &&     apt install -y bash tini libc6 libpam-modules krb5-user libnss3 procps &&     mkdir -p /opt/spark &&     mkdir -p /opt/spark/examples &&     mkdir -p /opt/spark/work-dir &&     touch /opt/spark/RELEASE &&     rm /bin/sh &&     ln -sv /bin/bash /bin/sh &&     echo "auth required pam_wheel.so use_uid" >> /etc/pam.d/su &&     chgrp root /etc/passwd && chmod ug+rw /etc/passwd &&     rm -rf /var/cache/apt/*' returned a non-zero code: 100
Failed to build Spark JVM Docker image, please refer to Docker build output for details.
```

Then I tried to load it manually.

```bash
docker-image-tool -t arseni build
minikube image load spark:arseni
kubectl run myspark --image=spark:arseni
kubectl get pods
```

```bash
NAME                              READY   STATUS             RESTARTS      AGE
myspark                           0/1     CrashLoopBackOff   2 (14s ago)   34s
```

```bash
kubectl logs myspark
```

Which gives the following.

```bash
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
Non-spark-on-k8s command provided, proceeding in pass-through mode...
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

