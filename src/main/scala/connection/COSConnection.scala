package connection

import org.apache.spark.sql.{DataFrame, Dataset, SaveMode}
import util.Spark

class COSConnection(conf: Map[String, String]) {
    private val hadoopConf = Spark.sparkSession.sparkContext.hadoopConfiguration
    private val service = conf("spark.cos.service")
    private val bucket = conf("spark.cos.bucket")+s".$service/$service"

    hadoopConf.set("fs.stocator.scheme.list", "cos")
    hadoopConf.set("fs.cos.impl", "com.ibm.stocator.fs.ObjectStoreFileSystem")
    hadoopConf.set("fs.stocator.cos.impl", "com.ibm.stocator.fs.cos.COSAPIClient")
    hadoopConf.set("fs.stocator.cos.scheme", "cos")

    hadoopConf.set(s"fs.cos.$service.access.key", conf("spark.cos.access.key"))
    hadoopConf.set(s"fs.cos.$service.secret.key", conf("spark.cos.secret.key"))
    hadoopConf.set(s"fs.cos.$service.endpoint",   conf("spark.cos.endpoint"))
    hadoopConf.set(s"fs.cos.$service.bucket",     conf("spark.cos.bucket"))

    def save(df: DataFrame, filename: String): Boolean = {
        df.write
          .mode(SaveMode.Overwrite)
          .option("header", "true")
          .csv(s"cos://$bucket/$filename")
        true
    }

    def read(filename: String): DataFrame = {
        Spark.sparkSession.read.option("header", "true").csv(s"cos://$bucket/$filename")
    }

    def listFiles(): Array[String] = {
        Spark.sparkSession.read.textFile(s"cos://$bucket/").inputFiles
    }
}
