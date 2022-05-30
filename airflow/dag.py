"""
Example Airflow DAG to submit Apache Spark applications using
`SparkSubmitOperator`, `SparkJDBCOperator` and `SparkSqlOperator`.
"""
from datetime import datetime

from airflow.models import DAG
from airflow.operators.python import PythonOperator, BranchPythonOperator
from airflow.operators.empty import EmptyOperator
from airflow.providers.apache.spark.operators.spark_submit import SparkSubmitOperator
from airflow.providers.telegram.operators.telegram import TelegramOperator
from airflow.providers.jdbc.hooks.jdbc import JdbcHook
from airflow.providers.jdbc.operators.jdbc import JdbcOperator
from airflow.utils.edgemodifier import Label
import logging

handler = logging.StreamHandler()
formatter = logging.Formatter('DAG::%(levelname)s:: %(message)s')
handler.setFormatter(formatter)

_LOGGER = logging.getLogger(__name__)
_LOGGER.addHandler(handler)
_LOGGER.setLevel(logging.INFO)

def _test_connection():
    connection = JdbcHook(conn_name_attr="db2", jdbc_conn_id="db2")
    result, message = connection.test_connection()
    _LOGGER.info(message)
    return "table_exists" if result else "tg_failure"

def _table_exists():
    connection = JdbcHook(conn_name_attr="db2", jdbc_conn_id="db2")
    result = connection.run("SELECT * FROM {{ table }} LIMIT 1")
    _LOGGER.info(result)
    return bool(result)


with DAG(
    dag_id='my_app',
    schedule_interval=None,
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
        task_id="fill_table"
    )

    calculate_sum = SparkSubmitOperator(
        task_id="calculate"
    )

    snapshot_table = EmptyOperator(
        task_id="cos_snapshot"
    )

    telegram_send_success = TelegramOperator(
        task_id="tg_success",
        trigger_rule="none_failed_or_skipped"
    )

    telegram_send_failure = TelegramOperator(
        task_id="tg_failure"
    )

    test_connection >> Label("failure") >> telegram_send_failure
    test_connection >> Label("success") >> table_exists >> snapshot_table >> telegram_send_success
    table_exists >> fill_table >> calculate_sum >> snapshot_table >> telegram_send_success
