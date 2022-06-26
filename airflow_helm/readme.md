# Description
I took standard airflow chart.
```bash
helm repo add apache-airflow https://airflow.apache.org
helm pull apache-airflow/airflow
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

# Problemes
I don't know how to add connections and variables using chart's values.yalm,
I guess they are stored in postgres.

---

I suspect it's not quite normal behaviour for scheduler.
```text
NAME                                 READY   STATUS    RESTARTS         AGE
pod/air-postgresql-0                 1/1     Running   1 (59m ago)      23h
pod/air-scheduler-0                  3/3     Running   48 (5m47s ago)   23h
pod/air-statsd-6876fcf78-wdgz4       1/1     Running   1 (59m ago)      23h
pod/air-triggerer-576579cfb8-djjcl   2/2     Running   2 (59m ago)      23h
pod/air-webserver-77bdcbf4bc-wt69g   1/1     Running   1 (59m ago)      23h
```

---

Still trying to make working airflow image.
