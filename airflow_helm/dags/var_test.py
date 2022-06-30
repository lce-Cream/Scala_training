from datetime import datetime, timedelta

from airflow.models import DAG
from airflow.operators.python import PythonOperator

def _test() -> bool:
    """Tests variables availablility."""
    from airflow.models import Variable

    var = Variable.get("test_var", deserialize_json=True)
    print(var)

    return True


with DAG(
    dag_id='my_var_test',
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
