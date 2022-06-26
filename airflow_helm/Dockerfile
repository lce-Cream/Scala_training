FROM apache/airflow

RUN pip install apache-airflow[spark]
RUN pip install apache-airflow[jdbc]
RUN pip install apache-airflow[telegram]

COPY ./jars/* /app/
COPY ./dag.py /opt/airflow/dags
