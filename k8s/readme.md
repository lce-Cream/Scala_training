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

Here is all debug info. All logs are provided in .logs files in this directory.
All I understood is `Failed to connect to master spark-master:7077`.

Spark account settings.

```bash
kubectl.exe describe serviceaccount spark
```

```text
Name:                spark
Namespace:           default
Labels:              <none>
Annotations:         <none>
Image pull secrets:  <none>
Mountable secrets:   spark-token-x6t7x
Tokens:              spark-token-x6t7x
Events:              <none>
```
Getting cluster IP.

```bash
kubectl.exe cluster-info
```

```text
Kubernetes control plane is running at https://127.0.0.1:55437
CoreDNS is running at https://127.0.0.1:55437/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy

To further debug and diagnose cluster problems, use 'kubectl cluster-info dump'.
```

Launching spark-submit from host machine.

```powershell
spark-submit.cmd `
--master k8s://https://127.0.0.1:55437 `
--deploy-mode cluster `
--name test `
--class org.apache.spark.examples.SparkPi `
--conf spark.kubernetes.container.image=bitnami/spark `
--conf spark.executor.instances=1 `
--conf spark.kubernetes.authenticate.driver.serviceAccountName=spark `
--conf spark.kubernetes.driver.pod.name=myspark `
local:///opt/bitnami/spark/examples/jars/spark-examples_2.12-3.2.1.jar 5
```


```bash
kubectl.exe get all
```

```text
NAME                READY   STATUS    RESTARTS      AGE
pod/myspark         1/1     Running   1 (21h ago)   24h
pod/spark-example   0/1     Error     0             110s

NAME                                       TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)                      AGE
service/kubernetes                         ClusterIP   10.96.0.1    <none>        443/TCP                      2d5h
service/test-11ef4b80d8e3fd0e-driver-svc   ClusterIP   None         <none>        7078/TCP,7079/TCP,4040/TCP   109s
```


```bash
kubectl.exe describe pod/spark-example
```

```text
Name:         spark-example
Namespace:    default
Priority:     0
Node:         minikube/192.168.49.2
Start Time:   Wed, 18 May 2022 23:37:42 +0300
Labels:       spark-app-selector=spark-8edff27c7fa24ab8b87e457780868b02
              spark-role=driver
Annotations:  <none>
Status:       Failed
IP:           172.17.0.4
IPs:
  IP:  172.17.0.4
Containers:
  spark-kubernetes-driver:
    Container ID:  docker://dc3506108a30f6d9e3fef402e8873b256bb3db6e6aa62e2abafe96b278b8a055
    Image:         bitnami/spark
    Image ID:      docker-pullable://bitnami/spark@sha256:2177fd7c037d321eb01e4daf54b5cd4a5a16a75b3bf81a8e50061f8e7e0280cb
    Ports:         7078/TCP, 7079/TCP, 4040/TCP
    Host Ports:    0/TCP, 0/TCP, 0/TCP
    Args:
      driver
      --properties-file
      /opt/spark/conf/spark.properties
      --class
      org.apache.spark.examples.SparkPi
      local:///opt/bitnami/spark/examples/jars/spark-examples_2.12-3.2.1.jar
      5
    State:          Terminated
      Reason:       Error
      Exit Code:    1
      Started:      Wed, 18 May 2022 23:37:44 +0300
      Finished:     Wed, 18 May 2022 23:38:46 +0300
    Ready:          False
    Restart Count:  0
    Limits:
      memory:  1408Mi
    Requests:
      cpu:     1
      memory:  1408Mi
    Environment:
      SPARK_USER:                 user
      SPARK_APPLICATION_ID:       spark-8edff27c7fa24ab8b87e457780868b02
      SPARK_DRIVER_BIND_ADDRESS:   (v1:status.podIP)
      SPARK_LOCAL_DIRS:           /var/data/spark-a56ea80c-68ca-42c6-8bb6-80cd0f8ebb59
      SPARK_CONF_DIR:             /opt/spark/conf
    Mounts:
      /opt/spark/conf from spark-conf-volume-driver (rw)
      /var/data/spark-a56ea80c-68ca-42c6-8bb6-80cd0f8ebb59 from spark-local-dir-1 (rw)
      /var/run/secrets/kubernetes.io/serviceaccount from kube-api-access-ffqcz (ro)
Conditions:
  Type              Status
  Initialized       True
  Ready             False
  ContainersReady   False
  PodScheduled      True
Volumes:
  spark-local-dir-1:
    Type:       EmptyDir (a temporary directory that shares a pod's lifetime)
    Medium:
    SizeLimit:  <unset>
  spark-conf-volume-driver:
    Type:      ConfigMap (a volume populated by a ConfigMap)
    Name:      spark-drv-90a84780d8e4009c-conf-map
    Optional:  false
  kube-api-access-ffqcz:
    Type:                    Projected (a volume that contains injected data from multiple sources)
    TokenExpirationSeconds:  3607
    ConfigMapName:           kube-root-ca.crt
    ConfigMapOptional:       <nil>
    DownwardAPI:             true
QoS Class:                   Burstable
Node-Selectors:              <none>
Tolerations:                 node.kubernetes.io/not-ready:NoExecute op=Exists for 300s
                             node.kubernetes.io/unreachable:NoExecute op=Exists for 300s
Events:
  Type     Reason       Age                    From               Message
  ----     ------       ----                   ----               -------
  Normal   Scheduled    4m25s                  default-scheduler  Successfully assigned default/spark-example to minikube
  Warning  FailedMount  4m25s                  kubelet            MountVolume.SetUp failed for volume "spark-conf-volume-driver" : configmap "spark-drv-90a84780d8e4009c-conf-map" not found
  Normal   Pulled       4m24s                  kubelet            Container image "bitnami/spark" already present on machine
  Normal   Created      4m24s                  kubelet            Created container spark-kubernetes-driver
  Normal   Started      4m24s                  kubelet            Started container spark-kubernetes-driver
  Warning  FailedMount  3m21s (x2 over 3m21s)  kubelet            MountVolume.SetUp failed for volume "spark-conf-volume-driver" : object "default"/"spark-drv-90a84780d8e4009c-conf-map" not registered
```

```bash
kubectl.exe describe service/test-11ef4b80d8e3fd0e-driver-svc
```

```text
Name:              test-11ef4b80d8e3fd0e-driver-svc
Namespace:         default
Labels:            <none>
Annotations:       <none>
Selector:          spark-app-selector=spark-8edff27c7fa24ab8b87e457780868b02,spark-role=driver
Type:              ClusterIP
IP Family Policy:  SingleStack
IP Families:       IPv4
IP:                None
IPs:               None
Port:              driver-rpc-port  7078/TCP
TargetPort:        7078/TCP
Endpoints:         <none>
Port:              blockmanager  7079/TCP
TargetPort:        7079/TCP
Endpoints:         <none>
Port:              spark-ui  4040/TCP
TargetPort:        4040/TCP
Endpoints:         <none>
Session Affinity:  None
Events:            <none>
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

