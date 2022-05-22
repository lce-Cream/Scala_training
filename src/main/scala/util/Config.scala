package util

import scala.collection.immutable.ListMap

object Config {
    val DB2Credentials: Map[String, String] = Map(
        "spark.db2.url"      -> "jdbc:db2://qwerty.databases.appdomain.cloud:30699/bludb:sslConnection=true;",
        "spark.db2.user"     -> "qwerty",
        "spark.db2.password" -> "qwerty",
        "spark.db2.table"    -> "ARSENI_SALES_TABLE",
        "spark.db2.driver"   -> "com.ibm.db2.jcc.DB2Driver",
    )

    val COSCredentials: Map[String, String] = Map(
        "spark.cos.access.key"     -> "qwerty",
        "spark.cos.secret.key"     -> "qwerty",
        "spark.cos.endpoint"       -> "s3.fra.eu.cloud-object-storage",
        "spark.cos.bucket"         -> "iba-ats-training-d3bf1ce5",
        "spark.cos.service"        -> "arseni",
    )

    val MySQLCredentials: Map[String, String] = Map(
        "spark.mysql.url"      -> "jdbc:mysql://localhost:3306/sample_database",
        "spark.mysql.user"     -> "root",
        "spark.mysql.password" -> "root",
        "spark.mysql.table"    -> "data",
    )

    val LocalCredentials: Map[String, String] = Map(
        "spark.local.path" -> "./data",
    )

    def getConfig: Map[String, String] = {
        val config =
            DB2Credentials     ++
            COSCredentials     ++
            MySQLCredentials   ++
            LocalCredentials   ++
            Environment.getMap ++
            Spark.sparkSession.conf.getAll

        // sort it all for better look
        ListMap(config.toSeq.sortBy(_._1):_*)
    }
}
