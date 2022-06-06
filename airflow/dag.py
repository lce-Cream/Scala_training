"""
Example Airflow DAG to submit Apache Spark applications using
`SparkSubmitOperator`, `SparkJDBCOperator` and `SparkSqlOperator`.
"""
from datetime import datetime, timedelta

from airflow.models import DAG
from airflow.operators.python import PythonOperator, BranchPythonOperator
from airflow.operators.empty import EmptyOperator
from airflow.providers.apache.spark.operators.spark_submit import SparkSubmitOperator
from airflow.providers.telegram.operators.telegram import TelegramOperator
from airflow.providers.jdbc.hooks.jdbc import JdbcHook
from airflow.providers.jdbc.operators.jdbc import JdbcOperator
from airflow.utils.edgemodifier import Label
from airflow.models import Variable


DB2_CONN_ID = "db2_default"
CONFIG = Variable.get("db2_config", deserialize_json=True)


def _test_connection(*args):
    hook = JdbcHook(jdbc_conn_id=DB2_CONN_ID)
    hook._test_connection_sql = "VALUES 1"
    result, message = hook.test_connection()
    print(message)
    return "table_exists" if result else "tg_failure"


def _table_exists():
    table = CONFIG["table"]

    hook = JdbcHook(jdbc_conn_id=DB2_CONN_ID)
    hook._test_connection_sql = f"SELECT 1 FROM {table}"
    result, message = hook.test_connection()
    print(message)
    return "cos_snapshot" if result else "fill_table"


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
        task_id="fill_table"
    )

    calculate_sum = SparkSubmitOperator(
        task_id="calculate"
    )

    snapshot_table = EmptyOperator(
        task_id="cos_snapshot"
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

    test_connection >> Label("failure") >> telegram_send_failure
    test_connection >> Label("success") >> table_exists >> Label("success") >> snapshot_table >> telegram_send_success
    table_exists    >> Label("failure") >> fill_table   >> calculate_sum    >> snapshot_table >> telegram_send_success
