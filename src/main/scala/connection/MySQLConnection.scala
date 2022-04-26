package connection

import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, SaveMode}
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import util.{Spark, Util}

import java.sql.{Connection, DriverManager, ResultSet, SQLException}
import java.util.Properties

class MySQLConnection(conf: Map[String, String]) extends AutoCloseable {
    private val logger = Logger.getLogger(getClass)

    private val url      = conf("url")
    private val user     = conf("user")
    private val password = conf("password")
    private val driver   = conf("driver")

    if (!url.startsWith("jdbc:mysql:")) throw new SQLException(s"driver is not yet supported: $url")

    private val connection: Connection = DriverManager.getConnection(url, user, password)

    /** Reads DataFrame from specified table.
     *
     *  @param table table name to read data from.
     *  @param partitionColumn  the name of a column that will be used for partitioning.
     *  @param lowerBound the minimum value of columnName used to decide partition stride.
     *  @param upperBound the maximum value of columnName used to decide partition stride.
     *  @param numPartitions the number of partitions.
     *  @return DataFrame.
     */
    def read(table: String,
             partitionColumn: Option[String] = None,
             lowerBound: Option[Long] = None,
             upperBound: Option[Long] = None,
             numPartitions: Option[Int] = None): DataFrame = {

        //        if (!tableExists(table)) throw new SQLException(s"table: $table does not exist")

        val properties = new Properties()
        properties.setProperty("driver", driver)
        properties.setProperty("user", user)
        properties.setProperty("password", password)

        properties.setProperty(
            JDBCOptions.JDBC_BATCH_FETCH_SIZE,
            conf.getOrElse(JDBCOptions.JDBC_BATCH_FETCH_SIZE, "10000")
        )

        logger.info(s"Reading table: $table")

        if (partitionColumn.isDefined && lowerBound.isDefined && upperBound.isDefined) {
            Spark.sparkSession.read.jdbc(
                url,
                table,
                partitionColumn.get,
                lowerBound.get,
                upperBound.get,
                numPartitions.get,
                properties
            )
        } else {
            Spark.sparkSession.read.jdbc(url, table, properties)
        }
    }

    /** Writes passed DataFrame to specified table.
     *
     *  @param table table name to write DataFrame to.
     *  @param df DataFrame which is being written.
     *  @param saveMode Append/Overwrite/Ignore/errorIfExists.
     *  @return DataFrame.
     */
    def write(table: String,
              df: DataFrame,
              saveMode: SaveMode = SaveMode.Append): Boolean = {

        val properties = new Properties()
        properties.setProperty("driver", driver)
        properties.setProperty("user", user)
        properties.setProperty("password", password)

        properties.setProperty(
            JDBCOptions.JDBC_BATCH_INSERT_SIZE,
            conf.getOrElse(JDBCOptions.JDBC_BATCH_INSERT_SIZE, "10000")
        )
        //        properties.setProperty(
        //            JDBCOptions.JDBC_TRUNCATE,
        //            config.getOrElse(JDBCOptions.JDBC_TRUNCATE, false).toString
        //        )

        val start = System.currentTimeMillis
        df.write.mode(saveMode).jdbc(url, table, properties)
        logger.info(s"Writing data to the $table table finished in ${Util.getDuration(start)}")
        true
    }

    /** Calculates cardinality of specified table (number of rows).
     *
     *  @param table table name.
     *  @return Long.
     */
    def getCount(table: String): Long = {
        val start = System.currentTimeMillis
        val query = s"SELECT COUNT(*) as number FROM $table"
        val rs = connection.createStatement.executeQuery(query)
        logger.info(s"Counting rows from $table finished in ${Util.getDuration(start)}")
        rs.next
        rs.getInt("number")
    }

    /** Executed passed SQL query.
     *
     *  @param query SQL query.
     *  @return ResultSet.
     */
    def executeQuery(query: String): ResultSet = {
        val start = System.currentTimeMillis
        val rs = connection.createStatement.executeQuery(query)
        logger.info(s"Execution of $query finished in ${Util.getDuration(start)}")
        rs
    }

    def _tableExists(table: String): Boolean = {
        // does not work
        val result = connection.getMetaData.getTables(null, null, table, null).next
        connection.commit()
        result
    }

    /** Closes the connection. */
    override def close(): Unit = {
        connection.close()
    }
}

