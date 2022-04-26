import connection.{COSConnection, DB2Connection, LocalConnection, MySQLConnection}
import dataGeneration.DataFrameGenerator
import dataTransform.Transform.calculateYearSales
import util.DefaultConfig
import util.Spark

object Main extends App{
    val config = Spark.sparkSession.conf.getAll // get spark config
    val df = DataFrameGenerator.generateSalesConcise(10) // generate sales DataFrame
    val df_annual = calculateYearSales(df) // transform it by calculating annual sales

    // db2
    val DB2Connection = new DB2Connection(DefaultConfig.DB2Credentials ++ config) // create a connection
    if (DB2Connection.write("sales_data", df_annual)){ // write to your connection
        DB2Connection.read("sales_data").show() // read from the connection and show
        println(s"Rows inserted: ${DB2Connection.getCount("sales_data")}") // count number of inserted rows
    }

    // cos
    val COSConnection = new COSConnection(DefaultConfig.COSCredentials ++ config) // create a connection
    if (COSConnection.save(df_annual, "arseni-storage", "sales.csv")){ // write to the connection
        COSConnection.read("arseni-storage", "sales.csv").show // read and show
        COSConnection.listFiles("arseni-storage").foreach(println) // list all files in cos
    }

    //mysql
    val MySQLConnection = new DB2Connection(DefaultConfig.MySQLCredentials ++ config) // create a connection
    if (MySQLConnection.write("sales_data", df_annual)){ // write to your connection
        MySQLConnection.read("sales_data").show() // read from connection and show
        println(s"Rows inserted: ${MySQLConnection.getCount("sales_data")}") // count number of inserted rows
    }

    // local
    val LocalConnection = new LocalConnection(DefaultConfig.LocalCredentials ++ config) // create a connection
    if (LocalConnection.save(df, "test.csv", "overwrite")){ // write to the connection
        LocalConnection.read("test.csv").show // read and show from the connection
    }
}
