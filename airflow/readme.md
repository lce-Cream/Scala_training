# Airflow setup
I tried every possible way, the most convenient and, most importantly, working one is based on WSL.

1. Install WSL (skip if you are on Linux).


2. Run these commands to install all necessaries.
    ```bash
    apt update && apt upgrade;
    apt install default-jdk;
    apt install pip;
    pip install apache-airflow;
    pip install apache-airflow[spark];
    pip install apache-airflow[jdbc];
    pip install apache-airflow[telegram];
    ```

3. Check your env
    ```bash
    env | sort | grep HOME
    ```
    Set `SPARK_HOME` if it's not already set.
    ```bash
    export SPARK_HOME="/mnt/d/software/Spark/spark-3.1.3-bin-hadoop3.2"
    ```
    And add it to path.
    ```bash
    export PATH=$SPARK_HOME/bin:$PATH; source ~/.bashrc
    ```
   Just to be sure.
   ```bash
   "export SPARK_LOCAL_IP=127.0.0.1; export SPARK_MASTER_HOST=127.0.0.1" >> ~/.bashrc
   ```
    By the way you can't use your java installed in Windows and setting it in path like Spark above wont work.


5. Tweak airflow config (optional). Use ctrl+w for search.
    ```bash
    nano ~/airflow/airflow.cfg
    ```
    
    Set your dag folder: `dags_folder = /mnt/c/Users/user/Desktop/Scala_training/airflow`  
  
    And refresh it every 5 seconds: `dag_dir_list_interval = 5`  
  
7. Modify configs in ./json folder. It's better to turn off any anti-malware software from this step on. Launch Airflow.
    ```bash
    airflow standalone
    ```

# DAG graph
<img src="./graph.png">


# Run DAG
Set your credentials in airflow connections.  
Check your dag is in Airflow GUI and run it.


# Questions
When I tried to install Ubuntu and Mint virtual machines they worked awful, constant lags and crushes
for no reason. Is there any protection software involved, which is trying to put down my VM?
