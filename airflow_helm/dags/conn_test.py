from datetime import datetime, timedelta

from airflow.models import DAG
from airflow.operators.python import PythonOperator

def _test() -> bool:
    """Tests connections availablility."""
    from airflow.models import Connection

    conn = Connection.get_connection_from_secrets("test-conn")
    print(conn)

    return True


with DAG(
    dag_id='my_conn_test',
    schedule_interval=timedelta(hours=4),
    start_date=datetime(2021, 1, 1),
    catchup=False,
    tags=['app'],
) as dag:

    test = PythonOperator(
        task_id="test",
        python_callable=_test,
        
    )

    test
