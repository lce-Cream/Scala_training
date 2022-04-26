package connection

import org.apache.spark.sql.{DataFrame, Dataset, SaveMode}
import util.Spark

class COSConnection(conf: Map[String, String]) {
    private val hadoopConf = Spark.sparkSession.sparkContext.hadoopConfiguration
    private val service = "arseni"

    hadoopConf.set("fs.stocator.scheme.list", "cos")
    hadoopConf.set("fs.cos.impl", "com.ibm.stocator.fs.ObjectStoreFileSystem")
    hadoopConf.set("fs.stocator.cos.impl", "com.ibm.stocator.fs.cos.COSAPIClient")
    hadoopConf.set("fs.stocator.cos.scheme", "cos")

    hadoopConf.set(s"fs.cos.$service.access.key", conf("spark.cos.access.key"))
    hadoopConf.set(s"fs.cos.$service.secret.key", conf("spark.cos.secret.key"))
    hadoopConf.set(s"fs.cos.$service.endpoint",   conf("spark.cos.endpoint"))

    def save(df: DataFrame, bucket: String, filename: String): Boolean = {
        df.write
          .mode(SaveMode.Overwrite)
          .option("header", "true")
          .csv(s"cos://$bucket.$service/$filename")
        true
    }

    def read(bucket: String, filename: String): DataFrame = {
        Spark.sparkSession.read.csv(s"cos://$bucket.$service/$filename")
    }

    def listFiles(bucket: String): Array[String] = {
        Spark.sparkSession.read.textFile(s"cos://$bucket.$service/").inputFiles
    }
}
