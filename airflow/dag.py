"""
Airflow DAG to to load and transform data using Apache Spark application.
"""
from datetime import datetime, timedelta
import json

from airflow.models import DAG
from airflow.operators.python import PythonOperator, BranchPythonOperator
from airflow.operators.empty import EmptyOperator
from airflow.providers.apache.spark.operators.spark_submit import SparkSubmitOperator
from airflow.providers.telegram.operators.telegram import TelegramOperator
from airflow.providers.jdbc.hooks.jdbc import JdbcHook
from airflow.providers.jdbc.operators.jdbc import JdbcOperator
from airflow.utils.edgemodifier import Label
from airflow.models import Variable, Connection
from airflow.providers.amazon.aws.operators import s3 as COSOperator

DB2_CONN_ID = "db2_default"
SPARK_CONN_ID = "spark_default"
COS_CONN_ID = "cos_default"


def get_config() -> dict:
    """Assembles all credentials and configs into one dictionary."""
    app_config = Variable.get("app_config", deserialize_json=True)
    db2_connection = Connection.get_connection_from_secrets(DB2_CONN_ID)
    cos_connection = json.loads(Connection.get_connection_from_secrets(COS_CONN_ID).extra)

    config = {
        "env_vars": {
            "spark.db2.url":      db2_connection.host,
            "spark.db2.user":     db2_connection.login,
            "spark.db2.password": db2_connection.password,
            "spark.db2.table":    app_config["table"],

            "spark.cos.access.key": cos_connection["access.key"],
            "spark.cos.secret.key": cos_connection["secret.key"],
            "spark.cos.endpoint":   cos_connection["endpoint"],
            "spark.cos.bucket":     cos_connection["bucket"],
            "spark.cos.service":    cos_connection["service"],
        },
        **app_config["submit"],
    }
    return config



def _test_connection() -> str:
    hook = JdbcHook(jdbc_conn_id=DB2_CONN_ID)
    hook._test_connection_sql = "VALUES 1"
    result, message = hook.test_connection()
    print(message)
    return "table_exists" if result else "tg_failure"


def _table_exists() -> str:
    table = get_config()["env_vars"]["spark.db2.table"]

    hook = JdbcHook(jdbc_conn_id=DB2_CONN_ID)
    hook._test_connection_sql = f"SELECT 1 FROM {table}"
    result, message = hook.test_connection()
    print(message)
    return "cos_snapshot" if not result else "fill_table"


with DAG(
    dag_id='my_app',
    schedule_interval=None,
    # schedule_interval=timedelta(hours=4),
    start_date=datetime(2021, 1, 1),
    catchup=False,
    tags=['app']
) as dag:

    test_connection = BranchPythonOperator(
        task_id="test_connection",
        python_callable=_test_connection
    )

    table_exists = BranchPythonOperator(
        task_id="table_exists",
        python_callable=_table_exists
    )

    fill_table = SparkSubmitOperator(
        task_id="fill_table",
        conn_id=SPARK_CONN_ID,
        name='spark-app-fill',
        application_args=["-m", "db2", "-a", "write", "-n", "5"],
        verbose = True,
        **get_config()
    )

    calculate_sum = SparkSubmitOperator(
        task_id="calculate",
        conn_id=SPARK_CONN_ID,
        name='spark-app-calculate',
        application_args=["--calc"],
        verbose = True,
        **get_config()
    )

    snapshot_table = SparkSubmitOperator(
        task_id="cos_snapshot",
        conn_id=SPARK_CONN_ID,
        name='spark-app-snapshot',
        application_args = ["--snap"],
        verbose = True,
        **get_config()
    )

    telegram_send_success = PythonOperator(
        task_id="tg_success",
        python_callable=lambda:print("TG SUCC")
    )

    telegram_send_failure = PythonOperator(
        task_id="tg_failure",
        python_callable=lambda:print("TG FAIL")
    )

    # telegram_send_success = TelegramOperator(
    #     task_id="tg_success",
    #     trigger_rule="none_failed_or_skipped"
    # )

    # telegram_send_failure = TelegramOperator(
    #     task_id="tg_failure"
    # )

    test_connection >> Label("success") >> table_exists
    test_connection >> Label("failure") >> telegram_send_failure

    table_exists    >> Label("success") >> snapshot_table >> telegram_send_success
    table_exists    >> Label("failure") >> fill_table     >> calculate_sum >> snapshot_table >> telegram_send_success
