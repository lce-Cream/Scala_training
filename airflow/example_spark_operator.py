from datetime import datetime

from airflow.models import DAG
from airflow.providers.apache.spark.operators.spark_submit import SparkSubmitOperator


with DAG(
    dag_id='MY_DAG',
    schedule_interval=None,
    catchup=False,
    start_date=datetime(2021, 1, 1),
    tags=['test']
) as dag:
    submit_job = SparkSubmitOperator(
        task_id="Spark-Pi",
        conn_id='spark_default',
        name='spark-pi',
        application="/mnt/d/software/Spark/spark-3.1.3-bin-hadoop3.2/examples/jars/spark-examples_2.12-3.1.3.jar",
        conf={
            'spark.executor.instances': 3,
            'spark.executor.memory': '500m'
        },
        java_class="org.apache.spark.examples.SparkPi",
    )

    submit_job
