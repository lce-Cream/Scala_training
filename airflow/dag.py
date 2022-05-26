"""
Example Airflow DAG to submit Apache Spark applications using
`SparkSubmitOperator`, `SparkJDBCOperator` and `SparkSqlOperator`.
"""
from datetime import datetime

from airflow.models import DAG
from airflow.providers.apache.spark.operators.spark_submit import SparkSubmitOperator

with DAG(
    dag_id='my_app',
    schedule_interval=None,
    start_date=datetime(2021, 1, 1),
    catchup=False,
    tags=['app']
) as dag:
    submit_job = SparkSubmitOperator(
        task_id="app",
        conn_id='spark_default',
        name='spark-app',
        application="/mnt/c/Users/user/Desktop/Scala_training/jars/app.jar",
        jars="/mnt/c/Users/user/Desktop/Scala_training/jars/*",
        conf={
            'spark.executor.instances': 2,
            'spark.executor.memory': '1000m'
        },
        java_class="Main",
        application_args=["-m", "db2", "-a", "read", "-n", "5"],
        verbose=True,
        env_vars={
            "spark.db2.url": "jdbc:db2://qwerty.databases.appdomain.cloud:30756/bludb:sslConnection=true;",
            "spark.db2.user": "qwerty9",
            "spark.db2.password": "qwerty7",
            "spark.db2.table": "ARSENI_SALES_DATA",
        }
    )

    submit_job
