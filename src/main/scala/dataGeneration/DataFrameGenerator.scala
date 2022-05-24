package dataGeneration

import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.types.{IntegerType, StructField, StructType}

import scala.util.Random
import util.Spark
import util.Spark.sparkSession.implicits._

object DataFrameGenerator {
    /** Generates sample DataFrame with sales data.
     *
     *  @param size amount of DataFrame's rows.
     *  @return DataFrame.
     */
    def generateSales(size: Int): DataFrame = {
        val id = (1 to size).toIterator
        def rndProductGroup: Int = Random.nextInt(9)
        def rndYear: Int         = Random.nextInt(3) + 2015
        def rndMonthSales: Int      = Random.nextInt(100000)

        val df = Spark.sparkSession.sparkContext.parallelize(
            Seq.fill(size){(
              id.next,rndProductGroup,rndYear,
              rndMonthSales, rndMonthSales, rndMonthSales, rndMonthSales,
              rndMonthSales, rndMonthSales, rndMonthSales, rndMonthSales,
              rndMonthSales, rndMonthSales, rndMonthSales, rndMonthSales
            )}
        ).toDF(
            "product_id", "product_group", "year",
            "month_1", "month_2", "month_3", "month_4", "month_5", "month_6",
            "month_7", "month_8", "month_9", "month_10", "month_11", "month_12"
        )
        df
    }

    /** Generates DataFrame according to provided arguments.
     *
     *  @param size amount of DataFrame's rows.
     *  @param values sequence of functions to generate values for every column.
     *  @param columns coma separated column names.
     *  @return DataFrame.
     */
    def generate(size: Int, values: Seq[Object], columns: String): DataFrame = {
        val df = Spark.sparkSession.sparkContext.parallelize(
            Seq.fill(size){values}
        ).toDF(columns)
        df
    }

    /** Generates realistic DataFrame with 'concise' product/group combination
     * using deterministic function for product group field.
     *
     *  @param size amount DataFrame's rows.
     *  @return DataFrame.
     */
    def generateSalesConcise(size: Int): DataFrame = {
        def rndProductGroup(id: Int): Int = (id + 3) % 10
        def rndMonthSales: Int = Random.nextInt(100000)
        val yearRange = 2015 to 2018
        val forEachYear = size/(yearRange.last-yearRange.head)

        // initialize DataFrame with first row
        var df = Seq((0, 0, yearRange.head,
          rndMonthSales, rndMonthSales, rndMonthSales, rndMonthSales,
          rndMonthSales, rndMonthSales, rndMonthSales, rndMonthSales,
          rndMonthSales, rndMonthSales, rndMonthSales, rndMonthSales))

        // fill the rest of it
        for (year <- yearRange) {
            val shuffledIds = Random.shuffle((1 to size).toList).toIterator
            df ++= Seq.fill(forEachYear) {
                val id = shuffledIds.next
                (id, rndProductGroup(id), year, rndMonthSales, rndMonthSales,
                  rndMonthSales, rndMonthSales, rndMonthSales, rndMonthSales,
                  rndMonthSales, rndMonthSales, rndMonthSales, rndMonthSales,
                  rndMonthSales, rndMonthSales)
            }
        }
        df.toDF(
            "product_id", "product_group", "year",
            "month_1", "month_2", "month_3", "month_4", "month_5", "month_6",
            "month_7", "month_8", "month_9", "month_10", "month_11", "month_12"
        )
    }

    /** Generates small sample DataFrame for test purposes.
     *  @return DataFrame.
     */
    def testGenerate(): DataFrame={
        val someData = Seq(
            Row(1, 1, 2015, 10),
            Row(2, 3, 2016, 20),
            Row(3, 2, 2018, 30)
        )

        val someSchema = List(
            StructField("product_id", IntegerType, true),
            StructField("product_group", IntegerType, true),
            StructField("year", IntegerType, true),
            StructField("month_1", IntegerType, true)
        )

        val df = Spark.sparkSession.createDataFrame(
            Spark.sparkSession.sparkContext.parallelize(someData),
            StructType(someSchema)
        )
        df
    }
}
