package util

object DefaultConfig {
    val DB2Credentials = Map(
        "spark.db2.url"      -> "jdbc:db2://qwerty.databases.appdomain.cloud:30699/bludb:sslConnection=true;",
        "spark.db2.user"     -> "rsg",
        "spark.db2.password" -> "oOWz4"
    )

    val COSCredentials = Map(
        "spark.cos.access.key"     -> "5c4f",
        "spark.cos.secret.key"     -> "af39",
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
