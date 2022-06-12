package connection

import java.util.Properties
import java.sql.{Connection, DriverManager, ResultSet, SQLException}
import org.apache.log4j.Logger
import org.apache.spark.sql.{DataFrame, SaveMode}
import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions
import util.Util
import util.Spark

/** A connection to DB2 cloud database.
 *
 *  @param conf connection configuration, includes credentials.
 */
class DB2Connection(conf: Map[String, String]) extends AutoCloseable {
    private val logger = Logger.getLogger(getClass)
    private val url      = conf("spark.db2.url")
    private val user     = conf("spark.db2.user")
    private val password = conf("spark.db2.password")
    private val driver   = conf("spark.db2.driver")

    if (!url.startsWith("jdbc:db2:")) throw new SQLException(s"driver is not yet supported: $url")

    private val connection: Connection = DriverManager.getConnection(url, user, password)
    connection.setAutoCommit(true)

    /** Reads DataFrame from specified table.
     *
     *  @param table  database table name to read data from.
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
            Spark.sparkSession.read.option("header", "true").jdbc(url, table, properties)
        }
    }

    /** Writes passed DataFrame to specified table.
     *
     *  @param df DataFrame which is being written.
     *  @param table database table name to write DataFrame to.
     *  @param saveMode Append/Overwrite/Ignore/errorIfExists.
     *  @return DataFrame.
     */
    def write(df: DataFrame,
              table: String,
              saveMode: String = "overwrite"): Boolean = {

        val properties = new Properties()
        properties.setProperty("driver", driver)
        properties.setProperty("user", user)
        properties.setProperty("password", password)

        properties.setProperty(
            JDBCOptions.JDBC_BATCH_INSERT_SIZE,
            conf.getOrElse(JDBCOptions.JDBC_BATCH_INSERT_SIZE, "10000")
        )

        val start = System.currentTimeMillis
        df.write.mode(saveMode).jdbc(url, table, properties)
        logger.info(s"Writing data to the $table table finished in ${Util.getDuration(start)}")
        true
    }

    /** Calculates cardinality of specified table (number of rows).
     *
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
     * @param query SQL query.
     * @return ResultSet.
     */
    def executeQuery(query: String): Int = {
        val start = System.currentTimeMillis
        val rs = connection.createStatement.executeUpdate(query)
        logger.info(s"Execution of $query finished in ${Util.getDuration(start)}")
        rs
    }

    def dropTable(table: String): Unit = {
        executeQuery(s"DROP TABLE $table")
    }

    /** Closes the connection. */
    override def close(): Unit = {
        connection.close()
    }
}
