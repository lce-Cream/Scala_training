### Description
This application provides a few classes for work with IBM DB2 cloud storage, IBM COS (cloud object storage), local storage,
MySQL database and data generation.
Here you can read data from using various connection types, transform data in dataframes with aggregation,
save dataframes to your connections and run it all in spark-submit.

### Requirements
It's strongly recommended using these versions for stable work.
| Name | Version |
| ------ | ------ |
| Scala | 2.12.10 |
| Spark | 3.13    |
| Hadoop| 3.2     |
| SBT   | 1.6.2   |
| OS    | Windows |
| JDK   | 15      |

### Setup
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
Configuration can be set using config file, and it looks like this. Every value except of #spark ones is compulsory.
```text
#spark
spark.master                     local[1]
spark.driver.memory              1g

#db2
spark.db2.url                    jdbc:db2://qwerty.databases.appdomain.cloud:30699/bludb:sslConnection=true;
spark.db2.user                   rsg
spark.db2.password               oOW

#cos
spark.cos.access.key             b8fced
spark.cos.secret.key             9d7d8b
spark.cos.endpoint               s3.fra.eu.cloud-object-storage

#mysql
spark.mysql.url                  jdbc:mysql://localhost:3306/sample_database
spark.mysql.user                 root
spark.mysql.password             root
spark.mysql.table                data

#local
spark.local.path                 ./data
```

However, there are default config in project's DefaultConfig.scala file in case you want to keep config file shorter.
Every value in this config is overloaded by config file above.
```scala
package util

object DefaultConfig {
    val DB2Credentials = Map(
        "spark.db2.url"      -> "jdbc:db2://qwerty.databases.appdomain.cloud:30699/bludb:sslConnection=true;",
        "spark.db2.user"     -> "rsg",
        "spark.db2.password" -> "oOW"
    )

    val COSCredentials = Map(
        "spark.cos.access.key"     -> "b8fced",
        "spark.cos.secret.key"     -> "9d7d8b",
        "spark.cos.endpoint"       -> "s3.fra.eu.cloud-object-storage",
    )

    val MySQLCredentials = Map(
        "spark.mysql.url"      -> "jdbc:mysql://localhost:3306/sample_database",
        "spark.mysql.user"     -> "root",
        "spark.mysql.password" -> "root",
        "spark.mysql.table"    -> "data",
    )

    val LocalCredentials = Map(
        "spark.local.path" -> "./",
    )
}
```

### Usage
After config setup you are ready to go. Write your code in Main.scala. Example is listed below.

```scala
import connection.{COSConnection, DB2Connection, LocalConnection, MySQLConnection}
import dataGeneration.DataFrameGenerator
import dataTransform.Transform.calculateYearSales
import util.DefaultConfig
import util.Spark

object Main extends App{
    val config = Spark.sparkSession.conf.getAll // get spark config
    val df = DataFrameGenerator.generateSalesConcise(10) // generate sales DataFrame
    val df_annual = calculateYearSales(df) // transform it by calculating annual sales

    // db2
    val DB2Connection = new DB2Connection(DefaultConfig.DB2Credentials ++ config) // create a connection
    if (DB2Connection.write("sales_data", df_annual)){ // write to your connection
        DB2Connection.read("sales_data").show() // read from the connection and show
        println(s"Rows inserted: ${DB2Connection.getCount("sales_data")}") // count number of inserted rows
    }

    // cos
    val COSConnection = new COSConnection(DefaultConfig.COSCredentials ++ config) // create a connection
    if (COSConnection.save(df_annual, "arseni-storage", "sales.csv")){ // write to the connection
        COSConnection.read("arseni-storage", "sales.csv").show // read and show
        COSConnection.listFiles("arseni-storage").foreach(println) // list all files in cos
    }

    //mysql
    val MySQLConnection = new DB2Connection(DefaultConfig.MySQLCredentials ++ config) // create a connection
    if (MySQLConnection.write("sales_data", df_annual)){ // write to your connection
        MySQLConnection.read("sales_data").show() // read from connection and show
        println(s"Rows inserted: ${MySQLConnection.getCount("sales_data")}") // count number of inserted rows
    }

    // local
    val LocalConnection = new LocalConnection(DefaultConfig.LocalCredentials ++ config) // create a connection
    if (LocalConnection.save(df, "test.csv", "overwrite")){ // write to the connection
        LocalConnection.read("test.csv").show
    }
}
```

### Launch
In project run.
```shell
$ sbt run
```

Or spark-submit could be of assistance. But before that application needs to be packed in one jar file.
It can be done by IDE's artifact functionality or by using sbt assembly plugin.
```shell
$ sbt assebmly
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

Or do it shorter by using spark-config.conf file described in configuration step. Do not forget to pack it all in jar.
```text
spark-submit.cmd \
--properties-file ./spark-config.conf \
--class Main ./Application.jar
```

### Results
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
|         0|            0|2015|  29124|  43417|  17641|  65116|  93575|  98311|  21726|  12258|  73585|   64841|   29938|   39647|
|         9|            2|2015|  34465|  36641|  70746|   3548|  24954|  21608|  26315|  90033|  24468|   21910|   93072|   30045|
|         7|            0|2015|  82734|  61822|  35722|  15715|  22345|  60347|  74586|  16117|  64548|   53897|   36931|   15290|
|         1|            4|2015|  96309|  86832|  94942|   8651|  67846|  21691|  71130|  19224|   2666|   49639|   92046|   37864|
|         5|            8|2016|  56369|  70532|  95822|  64463|  88444|  27847|  82465|  34064|  75089|   51376|   59919|    1345|
|         2|            5|2016|  46201|  52451|  68953|  62398|  80911|  73837|  31209|  91905|  57747|   74450|   42990|   35768|
|        10|            3|2016|  98488|  99794|  40676|   2976|   7480|  49430|  67346|  90979|  37223|   99005|   20909|   79052|
|        10|            3|2017|  56254|  89821|   2417|  50504|  41849|  12096|  52473|  56456|   4089|   94611|    3172|   83758|
|         4|            7|2017|  18524|  18634|  71382|  96726|  48601|  31478|  77739|  18180|  44041|   81925|   27060|   38392|
|         8|            1|2017|   6354|  58020|  68143|  83999|  47890|  55812|  94050|  46854|  15140|    8730|   27283|   41610|
|         4|            7|2018|  53235|  39494|  42242|  31264|   1286|  39852|  36523|  49308|  94872|   64828|   53690|   40121|
|         9|            2|2018|  19074|  48425|  62350|    591|  69868|  80447|  55667|  33833|  44149|   68632|   12664|    5549|
|         6|            9|2018|   5199|  75774|  87679|  98818|  50472|  60126|  73773|  33827|  10723|   56549|   80289|   39071|
+----------+-------------+----+-------+-------+-------+-------+-------+-------+-------+-------+-------+--------+--------+--------+
```

Annual sales DataFrame example.
```text
+----------+-------------+----+------+
|product_id|product_group|year| sales|
+----------+-------------+----+------+
|         0|            0|2015|576445|
|         9|            2|2015|639530|
|         7|            0|2015|387136|
|        10|            3|2015|580348|
|         2|            5|2016|514118|
|         3|            6|2016|655356|
|         7|            0|2016|636318|
|        10|            3|2017|763737|
|         2|            5|2017|477024|
|         4|            7|2017|619339|
|         6|            9|2018|478395|
|         1|            4|2018|669409|
|         7|            0|2018|535329|
+----------+-------------+----+------+
```
