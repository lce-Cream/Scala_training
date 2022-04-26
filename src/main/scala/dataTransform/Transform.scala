package dataTransform

import org.apache.spark.sql.DataFrame
import util.Spark.sparkSession.implicits._

object Transform {
    /** Calculates total year sales over all 12 months for every row.
     *  @param df sales DataFrame.
     *  @return DataFrame.
     */
    def calculateYearSales(df: DataFrame): DataFrame = {
        df.rdd.map(row => {
            val yearSales = row.toSeq.takeRight(12).map(v => v.toString.toInt).sum
            (row.getInt(0), row.getInt(1), row.getInt(2), yearSales)
        }).toDF("product_id", "product_group", "year", "sales")
    }
}
