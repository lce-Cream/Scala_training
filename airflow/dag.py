"""
Airflow DAG to load and transform data using Apache Spark application.
"""
from datetime import datetime, timedelta

from airflow.models import DAG
from airflow.utils.edgemodifier import Label
from airflow.operators.bash import BashOperator
from airflow.operators.python import BranchPythonOperator
from airflow.providers.telegram.operators.telegram import TelegramOperator
from airflow.providers.apache.spark.operators.spark_submit import SparkSubmitOperator

DB2_CONN_ID = "my_db2"
SPARK_CONN_ID = "my_spark"
COS_CONN_ID = "my_cos"
TG_CONN_ID = "my_telegram"


def _get_config() -> dict:
    """Assemble all credentials and configs into one dictionary to pass it to spark-submit job."""
    import json
    from airflow.models import Variable, Connection

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
    """Try to connect to a database and execute simple query."""
    from airflow.providers.jdbc.hooks.jdbc import JdbcHook

    hook = JdbcHook(jdbc_conn_id=DB2_CONN_ID)
    hook._test_connection_sql = "VALUES 1"
    result, message = hook.test_connection()
    print(message)
    return "table_exists" if result else "tg_failure"


def _table_exists() -> str:
    """Check if table exists."""
    from airflow.providers.jdbc.hooks.jdbc import JdbcHook

    table = _get_config()["env_vars"]["spark.db2.table"]
    hook = JdbcHook(jdbc_conn_id=DB2_CONN_ID)
    hook._test_connection_sql = f"SELECT 1 FROM {table}"
    result, message = hook.test_connection()
    print(message)
    return "snapshot_table" if result else "fill_table"


with DAG(
    dag_id='my_app',
    schedule_interval=timedelta(hours=4),
    start_date=datetime(2021, 1, 1),
    catchup=False,
    tags=['app']
) as dag:

    load_vars_conns = BashOperator(
        task_id="load_vars_conns",
        bash_command=
        '''
            cd {{ var.json.app_config.cwd }}; 
            airflow variables import ./json/variables.json;
            airflow connections import ./json/connections.json
        '''
    )

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
        application_args=["-m", "db2", "-a", "write", "-n", "{{ var.json.app_config.num_records }}"],
        verbose=True,
        **_get_config()
    )

    calculate_sum = SparkSubmitOperator(
        task_id="calculate",
        conn_id=SPARK_CONN_ID,
        name='spark-app-calculate',
        application_args=["--calc"],
        verbose=True,
        **_get_config()
    )

    snapshot_table = SparkSubmitOperator(
        task_id="snapshot_table",
        conn_id=SPARK_CONN_ID,
        name='spark-app-snapshot',
        application_args=["--snap"],
        verbose=True,
        **_get_config()
    )

    telegram_send_success = TelegramOperator(
        task_id="tg_success",
        telegram_conn_id=TG_CONN_ID,
        text="DAG success: {{ run_id }}",
        trigger_rule="none_failed_or_skipped"
    )

    telegram_send_failure = TelegramOperator(
        task_id="tg_failure",
        telegram_conn_id=TG_CONN_ID,
        text="DAG failure: {{ run_id }}"
    )

    load_vars_conns >> test_connection

    test_connection >> Label("success") >> table_exists
    test_connection >> Label("failure") >> telegram_send_failure

    table_exists >> Label("success") >> snapshot_table >> telegram_send_success
    table_exists >> Label("failure") >> fill_table >> calculate_sum >> snapshot_table >> telegram_send_success
