package util

import org.apache.log4j.{Level, LogManager}
import org.apache.spark.sql.SparkSession

object Spark {
    val sparkSession: SparkSession = {
        LogManager.getLogger("org.apache.spark").setLevel(Level.ERROR)

        val spark = SparkSession.builder
//            .master("local[1]")
            .appName("Training application")
            .getOrCreate()
        spark
    }
}
