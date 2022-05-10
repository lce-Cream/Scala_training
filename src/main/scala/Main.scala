import connection.{COSConnection, DB2Connection, LocalConnection, MySQLConnection}
import org.apache.spark.sql.DataFrame
import dataGeneration.DataFrameGenerator
import dataTransform.DataFrameTransform.calculateYearSales
import util.DefaultConfig
import util.Environment
import util.Spark

import scala.util.control.Breaks._
import scala.util.Try

object Main extends App{
    val config = Environment.getMap ++ Spark.sparkSession.conf.getAll
    config.foreach(println)

    def readDB2(number: Int): Option[DataFrame] = {
        val table = config("spark.db2.table")
        try {
            val DB2Connection = new DB2Connection(DefaultConfig.DB2Credentials ++ config)
            Some(DB2Connection.read(table).limit(number))
        }
        catch {
            case e: Exception =>
                println(e.getMessage)
                None
        }
    }

    def writeDB2(number: Int): Boolean = {
        val table = config("spark.db2.table")

        try {
            val df = DataFrameGenerator.generateSalesConcise(number)
            val df_annual = calculateYearSales(df)
            val DB2Connection = new DB2Connection(DefaultConfig.DB2Credentials ++ config)
            DB2Connection.write(table, df_annual)
        }
        catch {
            case e: Exception =>
                println(e.getMessage)
                false
        }
    }

    def readCOS(number: Int): Option[DataFrame] = {
        try {
            val COSConnection = new COSConnection(DefaultConfig.COSCredentials ++ config)
            Some(COSConnection.read("sales.csv").limit(number))
        }
        catch {
            case e: Exception =>
                println(e.getMessage)
                None
        }
    }

    def writeCOS(number: Int): Boolean = {
        try {
            val df = DataFrameGenerator.generateSalesConcise(number)
            val df_annual = calculateYearSales(df)
            val COSConnection = new COSConnection(DefaultConfig.COSCredentials ++ config)
            COSConnection.save(df_annual, "sales.csv")
        }
        catch {
            case e: Exception =>
                println(e.getMessage)
                false
        }
    }

    def checkArguments(args: Array[String]): Boolean = {
        val mods = List("db2", "cos", "local")
        val actions = List("read", "write")
        Try(
            args.length == 3
              && mods.contains(args(0))
              && actions.contains(args(1))
              && args(2).toInt.isInstanceOf[Int]
              || List("help", "exit").contains(args(0))
        ).getOrElse(false)
    }

    // console event loop
    while (true){
        breakable {
            val args = scala.io.StdIn.readLine("$ ").split(" ")

            if (!checkArguments(args)) {
                println("Incorrect arguments.")
                println(help())
                break
            }

            if (args(0) == "exit") sys.exit(0)
            if (args(0) == "help") {
                println(help())
                break
            }

            val mode   = args(0)
            val action = args(1)
            val number = args(2).toInt

            mode match {
                case "db2" => {
                    action match {
                        case "read"  =>
                            val df = readDB2(number)
                            if(df.isDefined) df.get.show else println("JOB ABORTED")

                        case "write" =>
                            if(writeDB2(number)) println("DONE") else println("JOB ABORTED")
                    }
                }
                case "cos" => {
                    action match {
                        case "read"  =>
                            val df = readCOS(number)
                            if(df.isDefined) df.get.show else println("JOB ABORTED")

                        case "write" =>
                            if(writeCOS(number)) println("DONE") else println("JOB ABORTED")
                    }
                }
            }
        }
    }

    def help(): String = {
        """
          |Choose mode to run data load and data transform processes.
          |
          |Mods:
          |  db2      Launch process using IBM DB2.
          |  cos      Launch process using IBM COS.
          |  mysql    Launch process using MySQL.
          |  local    Launch process using local filesystem.
          |  exit     Exit this REPL.
          |
          |Actions:
          |  read    Read data.
          |  write   Write data.
          |
          |Examples:
          |  db2 read 10  // show 10 records from db2 storage.
          |  cos write 20 // write 20 records to cos storage.
          |""".stripMargin
    }

//    def processCOS(): Boolean = {
//        try{
//            val COSConnection = new COSConnection(DefaultConfig.COSCredentials ++ config)
//            COSConnection.save(df_annual, "sales.csv")
//            COSConnection.read("sales.csv").show
//            COSConnection.listFiles().foreach(println)
//            true
//        }
//        catch {
//            case e: Exception => {
//                println(e.getMessage)
//                false
//            }
//        }
//    }
//
//
//    def processMySQL(): Boolean = {
//        try{
//            val MySQLConnection = new MySQLConnection(DefaultConfig.MySQLCredentials ++ config)
//            MySQLConnection.write("sales_data", df_annual)
//            MySQLConnection.read("sales_data").show()
//            println(s"Rows inserted: ${MySQLConnection.getCount("sales_data")}")
//            true
//        }
//        catch {
//            case e: Exception => {
//                println(e.getMessage)
//                false
//            }
//        }
//    }
//
//    def processLocal(): Boolean = {
//        try{
//            val LocalConnection = new LocalConnection(DefaultConfig.LocalCredentials ++ config)
//            LocalConnection.save(df, "test.csv", "overwrite")
//            LocalConnection.read("test.csv").show
//            true
//        }
//        catch {
//            case e: Exception => {
//                println(e.getMessage)
//                false
//            }
//        }
//    }
}
