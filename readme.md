# Description
This application provides a few classes for work with IBM DB2 cloud storage, IBM COS, local storage,
MySQL and data generation.
Here you can read data from using various connection types, transform data in dataframes with aggregation,
save dataframes to your connections and run it all in spark-submit, docker or kubernetes.


# Requirements
It's strongly recommended using these versions for stable work.
| Name | Version |
| ------ | ------ |
| Scala | 2.12.10 |
| Spark | 3.13    |
| Hadoop| 3.2     |
| SBT   | 1.5.8   |
| JDK   | 15      |
| Docker| 4.7.1   |
| OS    | Windows |


# Setup
## Installation
Clone this repo.

```shell
$ git clone https://github.com/lce-Cream/Scala_training.git
```

Load all dependencies from build.sbt.
```shell
$ sbt reload
```

Follow further instructions.

## Configuration
Configuration files templates are in ./src/main/resources.
Configuration can be set using environment variables or by passing conf file to spark-submit
with --properties-file option. Every value except the ones under #spark is compulsory.

However, there is default config in project's DefaultConfig.scala file in case you want to keep config file shorter.
Every value in this config is overloaded by config file above.


# Usage
After config setup you are ready to go. Here's help message to get started.
```text
usage: Configure job to run data load and data transform processes
 -a,--action <str>   Choose to read/write data.
 -h,--help           Show help massage.
 -m,--mode <str>     Launch process using db2/cos/local.
 -n,--number <int>   Amount of records to use.
 -v,--verbose        Print debug info during execution.
```

Example set of arguments that reads 5 records from DB2.
```text
-m db2 -a read -n 5
```


# Run
There are three ways to run the application.

---

Note: Be aware that Windows is a bloated mess and commands that work in cmd may not work in powershell and vise versa.
Also, you can't use bash emulator like git-bash to run spark-submit on Windows because of incompatibility,
so you must type "spark-submit.cmd".

---

## SBT
In project run the following command, change it according to usage section. Do not forget to set your credentials in
environment variables or place them in utils.Config.scala file.
```shell
sbt "run -m db2 -a read -n 5"
```


## Docker
Build docker image.
```bash
docker build -t arseni/spark-app .
```

Run a container with your credentials specified in env file.
```bash
docker run -it --env-file .\spark-config.env arseni/spark-app spark-submit --class Main --jars /app/*, app.jar -m db2 -a read -n 5
```


## Spark-submit
Spark-submit could be of assistance. You can use already compiled jars in ./jars directory or do it yourself.
Jars can be made by IDE's artifact functionality or by using sbt package command.
```shell
sbt package
```

You can specify all spark-submit parameters directly like this. Here app.jar implies to be "fat jar" and you do not
need to specify --jars in spark-submit.
```shell
spark-submit.cmd `
--class Main `
--conf spark.db2.url=jdbc:db2://qwerty.databases.appdomain.cloud:30699/bludb:sslConnection=true; `
--conf spark.db2.user=rsg `
--conf spark.db2.password=oOWz `
--conf spark.cos.access.key=b8fced6daf `
--conf spark.cos.secret.key=9d7d8b39262aac `
--conf spark.cos.endpoint=s3.fra.eu.cloud-object-storage `
--conf spark.mysql.url=jdbc:mysql://localhost:3306/sample_database `
--conf spark.mysql.user=root `
--conf spark.mysql.password=root `
--conf spark.mysql.table=data `
--conf spark.local.path=./data `
./app.jar -m db2 -a read -n 5
```

Or do it shorter by using spark-config.conf file described in configuration step.
```shell
spark-submit.cmd `
--properties-file ./spark-config.conf `
--class Main ./app.jar -m db2 -a read -n 5
```

But it's easier to use shipped jars.
```bash
spark-submit.cmd `
--jars .\jars\*, `
--properties-file .\spark-config.conf `
--class Main .\jars\app.jar -m db2 -a read -n 5
```


# Results
Firstly month sales DataFrame is generated, after that it transforms by calculating annual sales,
then it gets written to specified connection, and finally it gets read from that connection.

Initial sales DataFrame example.
```text
+----------+-------------+----+-------+-------+-------+-------+-------+-------+-------+-------+-------+--------+--------+--------+
|product_id|product_group|year|month_1|month_2|month_3|month_4|month_5|month_6|month_7|month_8|month_9|month_10|month_11|month_12|
+----------+-------------+----+-------+-------+-------+-------+-------+-------+-------+-------+-------+--------+--------+--------+
|         0|            0|2015|  82095|  66067|  30103|  19478|  50010|  68358|  27845|  42090|  65491|   24904|   69173|   43187|
|         3|            6|2015|  16670|  48769|  91150|  96295|    367|  33167|  48657|  79230|  68853|    4192|   37697|   75773|
|         4|            7|2015|  74160|  46117|  58579|  42082|  68974|  49723|  37236|  28044|  89189|   39900|   97585|   72770|
|         7|            0|2015|  24844|  99512|  42147|  29964|  84566|  89246|  56309|   2751|  94987|   53043|   37171|   59447|
|         5|            8|2016|  60066|  60898|  66462|  48869|  49612|  64098|  93883|  70119|  65748|   38990|   92203|   75005|
|         8|            1|2016|  78809|  64925|  44594|   4929|  20204|  65512|   6475|   4528|  46148|   37558|   86701|   88103|
|         1|            4|2016|   6511|  51305|  90122|   7165|   7299|  80963|  24369|  28061|  56211|    8310|   39635|   49374|
|         7|            0|2017|  72013|  95369|  83562|  10640|  51611|  69538|  54113|  82519|  21771|   72815|   28781|    2607|
|         9|            2|2017|  33550|  19051|  54328|  87999|  50465|  89375|  75796|  96635|  73188|   92133|    5164|    6738|
|        10|            3|2017|  70713|  41863|  67815|  80292|  84341|  33958|  94553|  27089|  78310|   89463|   49093|   42636|
|         1|            4|2018|  12931|  89224|  17297|  53859|   4213|  44743|   6078|  69376|  42967|   66341|   63343|   69643|
|         8|            1|2018|  81677|  81357|   4932|  16838|  36576|  21515|  26704|  43229|  52222|   76292|    6119|   13583|
|         7|            0|2018|  43743|  79959|  60081|  27357|    803|  87663|  38560|  77741|   6935|   29986|   46334|   72891|
+----------+-------------+----+-------+-------+-------+-------+-------+-------+-------+-------+-------+--------+--------+--------+
```

Annual sales DataFrame example.
```text
+----------+-------------+----+------+
|product_id|product_group|year| sales|
+----------+-------------+----+------+
|         0|            0|2015|588801|
|         3|            6|2015|600820|
|         4|            7|2015|704359|
|         7|            0|2015|673987|
|         5|            8|2016|785953|
|         8|            1|2016|548486|
|         1|            4|2016|449325|
|         7|            0|2017|645339|
|         9|            2|2017|684422|
|        10|            3|2017|760126|
|         1|            4|2018|540015|
|         8|            1|2018|461044|
|         7|            0|2018|572053|
+----------+-------------+----+------+
```

# Known issues
Launch problems.  
I suspect jdk and jre versions are responsible for such unstable behaviour, yet another reason to use containers.  
Using sbt run or IntelliJ run button, app successfully reads data not every run, but when
using jars or image in minikube everything is perfectly fine.
```text
22/05/22 18:40:48 INFO DB2Connection: Reading table: ARSENI_SALES_TABLE
DB2 SQL Error: SQLCODE=-204, SQLSTATE=42704, SQLERRMC=DJC70779.ARSENI_SALES_TABLE, DRIVER=4.31.10
JOB ABORTED
```
```text
22/05/22 20:15:37 INFO ObjectStoreVisitor: Stocator registered as cos for cos://iba-ats-training-d3bf1ce5.arseni/arseni/sales.csv
22/05/22 20:15:37 INFO COSAPIClient: Init :  cos://iba-ats-training-d3bf1ce5.arseni/arseni/sales.csv
Unable to execute HTTP request: s3.fra.eu.cloud-object-storage
JOB ABORTED
```

---

Commands confusion.  
I wanted to use powershell only but this idea ended up in a mixture of
cmd, powershell and bash, each of them with its use cases. Some commands in examples work in cmd some in powershell,
and some neither. I need to test everything and develop kinda universal style. So when facing misbehaviour try to
write this command in one line without any break characters in cmd and powershell.
