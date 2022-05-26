### Launching spark example


*** Reading local file: /home/tia/airflow/logs/dag_id=example_spark_operator/run_id=manual__2022-05-26T01:03:51.417447+00:00/task_id=Spark-Pi/attempt=1.log
[2022-05-26, 07:03:53 +03] {taskinstance.py:1159} INFO - Dependencies all met for <TaskInstance: example_spark_operator.Spark-Pi manual__2022-05-26T01:03:51.417447+00:00 [queued]>
[2022-05-26, 07:03:53 +03] {taskinstance.py:1159} INFO - Dependencies all met for <TaskInstance: example_spark_operator.Spark-Pi manual__2022-05-26T01:03:51.417447+00:00 [queued]>
[2022-05-26, 07:03:53 +03] {taskinstance.py:1356} INFO - 
--------------------------------------------------------------------------------
[2022-05-26, 07:03:53 +03] {taskinstance.py:1357} INFO - Starting attempt 1 of 1
[2022-05-26, 07:03:53 +03] {taskinstance.py:1358} INFO - 
--------------------------------------------------------------------------------
[2022-05-26, 07:03:53 +03] {taskinstance.py:1377} INFO - Executing <Task(SparkSubmitOperator): Spark-Pi> on 2022-05-26 01:03:51.417447+00:00
[2022-05-26, 07:03:53 +03] {standard_task_runner.py:52} INFO - Started process 28021 to run task
[2022-05-26, 07:03:53 +03] {standard_task_runner.py:79} INFO - Running: ['airflow', 'tasks', 'run', 'example_spark_operator', 'Spark-Pi', 'manual__2022-05-26T01:03:51.417447+00:00', '--job-id', '25', '--raw', '--subdir', 'DAGS_FOLDER/MY_DAG.py', '--cfg-path', '/tmp/tmpjxxvgtl2', '--error-file', '/tmp/tmpzu62yi_3']
[2022-05-26, 07:03:53 +03] {standard_task_runner.py:80} INFO - Job 25: Subtask Spark-Pi
[2022-05-26, 07:03:53 +03] {task_command.py:369} INFO - Running <TaskInstance: example_spark_operator.Spark-Pi manual__2022-05-26T01:03:51.417447+00:00 [running]> on host ZHUKAUAT15.itd.iba.by
[2022-05-26, 07:03:53 +03] {taskinstance.py:1569} INFO - Exporting the following env vars:
AIRFLOW_CTX_DAG_OWNER=airflow
AIRFLOW_CTX_DAG_ID=example_spark_operator
AIRFLOW_CTX_TASK_ID=Spark-Pi
AIRFLOW_CTX_EXECUTION_DATE=2022-05-26T01:03:51.417447+00:00
AIRFLOW_CTX_TRY_NUMBER=1
AIRFLOW_CTX_DAG_RUN_ID=manual__2022-05-26T01:03:51.417447+00:00
[2022-05-26, 07:03:53 +03] {base.py:68} INFO - Using connection ID 'spark_default' for task execution.
[2022-05-26, 07:03:53 +03] {spark_submit.py:335} INFO - Spark-Submit cmd: spark-submit --master yarn --conf spark.executor.instances=3 --conf spark.executor.memory=500m --name spark-pi --class org.apache.spark.examples.SparkPi --queue root.default $SPARK_HOME/examples/jars/spark-examples_2.12-3.1.3.jar
[2022-05-26, 07:03:53 +03] {spark_submit.py:488} INFO - JAVA_HOME is not set
[2022-05-26, 07:03:53 +03] {taskinstance.py:1889} ERROR - Task failed with exception
Traceback (most recent call last):
  File "/home/tia/.local/lib/python3.8/site-packages/airflow/providers/apache/spark/operators/spark_submit.py", line 157, in execute
    self._hook.submit(self._application)
  File "/home/tia/.local/lib/python3.8/site-packages/airflow/providers/apache/spark/hooks/spark_submit.py", line 419, in submit
    raise AirflowException(
airflow.exceptions.AirflowException: Cannot execute: spark-submit --master yarn --conf spark.executor.instances=3 --conf spark.executor.memory=500m --name spark-pi --class org.apache.spark.examples.SparkPi --queue root.default $SPARK_HOME/examples/jars/spark-examples_2.12-3.1.3.jar. Error code is: 1.
[2022-05-26, 07:03:53 +03] {taskinstance.py:1395} INFO - Marking task as FAILED. dag_id=example_spark_operator, task_id=Spark-Pi, execution_date=20220526T010351, start_date=20220526T010353, end_date=20220526T010353
[2022-05-26, 07:03:53 +03] {standard_task_runner.py:92} ERROR - Failed to execute job 25 for task Spark-Pi (Cannot execute: spark-submit --master yarn --conf spark.executor.instances=3 --conf spark.executor.memory=500m --name spark-pi --class org.apache.spark.examples.SparkPi --queue root.default $SPARK_HOME/examples/jars/spark-examples_2.12-3.1.3.jar. Error code is: 1.; 28021)
[2022-05-26, 07:03:53 +03] {local_task_job.py:156} INFO - Task exited with return code 1
[2022-05-26, 07:03:53 +03] {local_task_job.py:273} INFO - 0 downstream tasks scheduled from follow-on schedule check
