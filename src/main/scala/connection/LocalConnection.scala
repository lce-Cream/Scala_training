package connection

import org.apache.spark.sql.{DataFrame, SaveMode}
import util.Spark

class LocalConnection(conf: Map[String, String]) {
    private val path = conf("path")
    private val modes = List("Overwrite", "Append", "Ignore")

    def save(df: DataFrame, name: String, mode: String): Boolean = {
        if (!modes.contains(mode)) throw new NoSuchMethodException(s"$mode is not supported, use Overwrite\\Append\\Ignore")

        df.write
          .mode(mode)
          .option("header", "true")
          .csv(s"$path/$name")
        true
    }

    def read(name: String): DataFrame = {
        Spark.sparkSession.read.csv(s"$path/$name")
    }

    def listFiles(): Array[String] = {
        Spark.sparkSession.read.textFile(s"$path").inputFiles
    }

}
