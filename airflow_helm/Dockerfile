FROM apache/airflow

USER root
# install java
RUN apt-get update \
  && apt-get install -y --no-install-recommends \
         openjdk-11-jre-headless \
  && apt-get autoremove -yqq --purge \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

USER airflow
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

# all required providers
RUN pip install --no-cache-dir apache-airflow[spark]
RUN pip install --no-cache-dir apache-airflow[jdbc]
RUN pip install --no-cache-dir apache-airflow[telegram]

COPY ./jars/* /app/
COPY ./creds /creds

RUN airflow db init
RUN airflow connections import /creds/connections.json
RUN airflow variables import /creds/variables.json

COPY ./dags/var_test.py /opt/airflow/dags
COPY ./dags/conn_test.py /opt/airflow/dags
