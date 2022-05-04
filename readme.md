## Description
This application provides a few classes for work with IBM DB2 cloud storage, IBM COS (cloud object storage), local storage,
MySQL database and data generation.
Here you can read data from using various connection types, transform data in dataframes with aggregation,
save dataframes to your connections and run it all in spark-submit.

## Requirements
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

## Setup
### Installation
Clone this repo.
```shell
$ git clone https://github.com/lce-Cream/Scala_training.git
```

Load all dependencies from build.sbt.
```shell
$ sbt reload
```

Follow further instructions.

### Configuration
Configuration can be set using environment variables, and it looks like this. Every value except the ones under #spark is compulsory.
```text
#spark
# spark.driver.memory=1g
spark.master=local[1]

#db2
spark.db2.url=jdbc:db2://qwerty123.databases.appdomain.cloud:30699/bludb:sslConnection=true;
spark.db2.user=qwerty123
spark.db2.password=qwerty123
spark.db2.table=ARSENI_SALES_DATA
spark.db2.driver=com.ibm.db2.jcc.DB2Driver

#cos
spark.cos.access.key=qwerty123
spark.cos.secret.key=qwerty123
spark.cos.endpoint=s3.fra.eu.cloud-object-storage
spark.cos.bucket=iba-ats-training-d3bf1ce5
spark.cos.service=arseni

#mysql
spark.mysql.url=jdbc:mysql://localhost:3306/sample_database
spark.mysql.user=root
spark.mysql.password=root
spark.mysql.table=data

#local
spark.local.path=./data
```

However, there is default config in project's DefaultConfig.scala file in case you want to keep config file shorter.
Every value in this config is overloaded by config file above.

## Run
There are three ways to run the application.
### SBT
In project run.
```shell
$ sbt run
```

### Spark-submit
Or spark-submit could be of assistance. But before that application needs to be packed in one jar file.
It can be done by IDE's artifact functionality or by using sbt package command.
```shell
$ sbt package
```

You can specify all spark-submit parameters directly like this.
```shell
spark-submit.cmd \
--class Main \
--master local[*] \
--conf spark.db2.url=jdbc:db2://qwerty.databases.appdomain.cloud:30699/bludb:sslConnection=true; \
--conf spark.db2.user=rsg \
--conf spark.db2.password=oOWz \
--conf spark.cos.access.key=b8fced6daf \
--conf spark.cos.secret.key=9d7d8b39262aac \
--conf spark.cos.endpoint=s3.fra.eu.cloud-object-storage \
--conf spark.mysql.url=jdbc:mysql://localhost:3306/sample_database \
--conf spark.mysql.user=root \
--conf spark.mysql.password=root \
--conf spark.mysql.table=data \
--conf spark.local.path=./data \
./Application.jar
```

Or do it shorter by using spark-config.conf file described in configuration step. Do not forget to pack source code in jar.
```text
spark-submit.cmd \
--properties-file ./spark-config.conf \
--class Main ./Application.jar
```

### Docker
Build docker image. Make necessary changes to a default dockerfile.
```dockerfile
FROM bitnami/spark:latest
# jar dependencies
COPY ./lib/* /application/lib/
# application source packed in jar
COPY ./out/artifacts/test_4_jar/test_4.jar /application/
# command to run spark submit job
CMD ["spark-submit",\
     "--jars", "/application/lib/*",\
     "--class", "Main",\
     "/application/test_4.jar"]
```

Build an image.
```bash
docker build -f ./Dockerfile --cache-from bitnami/spark:latest -t myapp:test_image .
```
Run container.
```bash
winpty docker run -it --env-file ./src/main/scala/spark-config.env --name myapp_test_env myapp:test_container
```
Winpty here is for Windows compatibility.

## Usage
After config setup you are ready to go. Here's help message to get started.
```text
Choose mode to run data load and data transform processes.

Mods:
  db2      Launch process using IBM DB2.
  cos      Launch process using IBM COS.
  mysql    Launch process using MySQL.
  local    Launch process using local filesystem.
  exit     Exit this REPL.

Actions:
  read    Read data.
  write   Write data.

Examples:
  db2 read 10  // show 10 records from db2 storage.
  cos write 20 // write 20 records to cos storage.
```

## Results
Every connection type code described above gives roughly the same result.
Firstly month sales DataFrame is generated,
after that it transforms by calculating annual sales,
then it gets written to specified connection,
and finally it gets read from that connection.

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
