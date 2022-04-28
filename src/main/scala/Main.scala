import connection.{COSConnection, DB2Connection, LocalConnection, MySQLConnection}

import dataGeneration.DataFrameGenerator
import dataTransform.DataFrameTransform.calculateYearSales

import util.DefaultConfig
import util.Spark
import util.Environment
import util.CLIParser

object Main extends App{
//    val config = Spark.sparkSession.conf.getAll // get spark config
    val config = Environment.getMap             // get spark config from environment variables

    val df = DataFrameGenerator.generateSalesConcise(10) // generate sales DataFrame
    val df_annual = calculateYearSales(df)               // transform it by calculating annual sales

    // console event loop
    while (true){
        val mode = scala.io.StdIn.readLine("$ ")

        mode match {
            case "db2"    => if (processDB2())   println("DONE") else println("JOB ABORTED")
            case "cos"    => if (processCOS())   println("DONE") else println("JOB ABORTED")
            case "mysql"  => if (processMySQL()) println("DONE") else println("JOB ABORTED")
            case "local"  => if (processLocal()) println("DONE") else println("JOB ABORTED")
            case "help"   => println(help())
            case "exit"   => sys.exit(0)
            case other    => {
                println(s"Unexpected mode: $other")
                println(help())
            }
        }
    }

    def help(): String = {
        """
          |Choose mode to run data load and data transform processes.
          |Commands:
          |  db2      Launch process using IBM DB2
          |  cos      Launch process using IBM COS
          |  mysql    Launch process using MySQL
          |  local    Launch process using local filesystem
          |  exit     Exit this REPL
          |""".stripMargin
    }

    def processDB2(): Boolean = {
        val table = config("spark.db2.table")

        try{
            val DB2Connection = new DB2Connection(DefaultConfig.DB2Credentials ++ config) // create a connection
//            DB2Connection.write(table, df_annual)                                  // write to your connection
            DB2Connection.read(table).show()                                       // read from the connection and show
//            println(s"Rows inserted: ${DB2Connection.getCount(table)}")            // count number of inserted rows
            true
        }
        catch {
            case e: Exception => {
                println(e.getMessage)
                false
            }
        }
    }

    def processCOS(): Boolean = {
        try{
            val COSConnection = new COSConnection(DefaultConfig.COSCredentials ++ config) // create a connection
//            COSConnection.save(df_annual, "sales.csv")                                    // write to the connection
            COSConnection.read("sales.csv").show                                          // read and show
            COSConnection.listFiles().foreach(println)                                    // list all files in cos
            true
        }
        catch {
            case e: Exception => {
                println(e.getMessage)
                false
            }
        }
    }

    def processMySQL(): Boolean = {
        try{
            val MySQLConnection = new MySQLConnection(DefaultConfig.MySQLCredentials ++ config) // create a connection
            MySQLConnection.write("sales_data", df_annual)                                    // write to your connection
            MySQLConnection.read("sales_data").show()                                         // read from connection and show
            println(s"Rows inserted: ${MySQLConnection.getCount("sales_data")}")              // count number of inserted rows
            true
        }
        catch {
            case e: Exception => {
                println(e.getMessage)
                false
            }
        }
    }

    def processLocal(): Boolean = {
        try{
            val LocalConnection = new LocalConnection(DefaultConfig.LocalCredentials ++ config) // create a connection
            LocalConnection.save(df, "test.csv", "overwrite")                                   // write to the connection
            LocalConnection.read("test.csv").show                                               // read and show from the connection
            true
        }
        catch {
            case e: Exception => {
                println(e.getMessage)
                false
            }
        }
    }
}
