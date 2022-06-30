# Description
I took standard airflow chart.
```bash
helm repo add apache-airflow https://airflow.apache.org
helm pull apache-airflow/airflow
```

Also, there is <a href="https://github.com/airflow-helm/charts/tree/main/charts/airflow">another airflow chart supplier</a> with thorough and friendly documentation, but I didn't manage to launch it despite seeming simplicity and countless tries.
```bash
helm repo add airflow-stable https://airflow-helm.github.io/charts
helm repo update
helm pull airflow-stable/airflow
```

And modified it by deleting and turning off some "unnecessary" stuff. Then I replaced original
apache/airflow image with my image, in which I installed all required operator providers.

# Usage
Start Docker and Minikube.
Build new airflow image and load it in Minikube.
```bash
docker build -t arseni/airflow .
minikube image load arseni/aiflow
```

This image can be tested, just run it and go to localhost:8080.
```bash
docker run --name air --rm -it -p 8080:8080 arseni/airflow standalone
```

Install airflow helm chart with your values specified. Here you set repository
info to sync dags from and airflow image to build pods on.
```bash
kubectl create namespace airflow
helm install air ./air-chart -n airflow \
--set dags.gitSync.repo=https://github.com/lce-Cream/Scala_training.git \
--set dags.gitSync.branch=dev \
--set dags.gitSync.subPath=airflow \
--set defaultAirflowRepository=arseni/airflow 
```

Don't forget to forward ports.
```bash
kubectl.exe port-forward svc/air-webserver 8080:8080 -n airflow
```

If air-chart/values.yalm gets any changes, use this to update the cluster.
```bash
helm upgrade air ./air-chart -f ./air-chart/values.yaml -n airflow
```

# Problems & Questions
I'm still trying to load connections and variables using chart's values.yalm. It feels impossible. I ended up conducting experiments with ./dags/*_test.py to find a way to achieve my goal. Until I figure it out, I have to bake creds into my docker image, but it doesn't work in k8s.

---

When airflow scheduler imports new DAG, all its functions, which are supposed to be executed on DAG runtime only (as I think), get executed. This results in queries to airflow variables and connections tables before they get populated in first DAG's task (commented in current dag.py). But such behavior breaks optimization logic, that states about moving all heavy operations to functions thus avoiding top level code. There is definitely some misunderstanding of this mechanism in my mind.

---

I suspect it is not quite normal behavior for scheduler, and it should be way more stable.
```text
NAME                                 READY   STATUS    RESTARTS         AGE
pod/air-postgresql-0                 1/1     Running   1 (59m ago)      23h
pod/air-scheduler-0                  3/3     Running   48 (5m47s ago)   23h
pod/air-statsd-6876fcf78-wdgz4       1/1     Running   1 (59m ago)      23h
pod/air-triggerer-576579cfb8-djjcl   2/2     Running   2 (59m ago)      23h
pod/air-webserver-77bdcbf4bc-wt69g   1/1     Running   1 (59m ago)      23h
```

---

What purpose does pod with postgres serve, what does it store? I think it's like sqlite in airflow standalone mode.

---

Last and olny time I successfully installed custom airflow helm chart, it took 15 mins of pods crushing and restarting. Is there a specific order in which pods must be ready to finish whole process, and before they align in right order you have to wait or what? And how do I check pods logs if they constantly restart. I don't get it.
