import connection.{COSConnection, DB2Connection, LocalConnection, MySQLConnection}
import org.apache.spark.sql.DataFrame
import dataGeneration.DataFrameGenerator
import dataTransform.DataFrameTransform.calculateYearSales
import util.Config
import util.CLIParser

object Main {
    private val config = Config.getConfig

    def readDB2(number: Int): Option[DataFrame] = {
        try {
            val DB2Connection = new DB2Connection(config)
            // probably not the most efficient way to do it
            Some(DB2Connection.read().limit(number))
        }
        catch {
            case e: Exception =>
                println(e.getMessage)
                None
        }
    }

    def writeDB2(number: Int): Boolean = {
        try {
            val df = DataFrameGenerator.generateSalesConcise(number)
//            val df_annual = calculateYearSales(df)
            val DB2Connection = new DB2Connection(config)
            DB2Connection.write(df)
        }
        catch {
            case e: Exception =>
                println(e.getMessage)
                false
        }
    }

    def readCOS(number: Int): Option[DataFrame] = {
        try {
            val COSConnection = new COSConnection(config)
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
            val COSConnection = new COSConnection(config)
            COSConnection.save(df_annual, "sales.csv")
        }
        catch {
            case e: Exception =>
                println(e.getMessage)
                false
        }
    }

    def readLocal(number: Int): Option[DataFrame] = {
        try {
            val LocalConnection = new LocalConnection(config)
            Some(LocalConnection.read("test.csv").limit(number))
        }
        catch {
            case e: Exception => {
                println(e.getMessage)
                None
            }
        }
    }

    def writeLocal(number: Int): Boolean = {
        try {
            val df = DataFrameGenerator.generateSalesConcise(number)
            val df_annual = calculateYearSales(df)
            val LocalConnection = new LocalConnection(config)
            LocalConnection.save(df_annual, "test.csv")
        }
        catch {
            case e: Exception => {
                println(e.getMessage)
                false
            }
        }
    }

    def calculate(): Boolean = {
        try {
            val DB2Connection = new DB2Connection(config)
            val df = DB2Connection.read()
            val df_annual = calculateYearSales(df)
            DB2Connection.write(df_annual, saveMode = "overwrite")
        }
        catch {
            case e: Exception =>
                println(e.getMessage)
                false
        }
    }

    def snapshot(): Boolean = {
        try {
            val DB2Connection = new DB2Connection(config)
            val COSConnection = new COSConnection(config)
            val df = DB2Connection.read()
            COSConnection.save(df, "snapshot.csv")
        }
        catch {
            case e: Exception =>
                println(e.getMessage)
                false
        }
    }

    def main(args: Array[String]): Unit = {
        val argsMap = CLIParser.parse(args)
        val mode    = argsMap("mode")
        val action  = argsMap("action")
        val number  = argsMap("number").toInt

        val calc = argsMap.contains("calc")
        val snap = argsMap.contains("snap")

        if (argsMap.contains("verbose")) config.foreach(println)

        if (calc) {
            if (calculate()) println("CALCULATION DONE") else println("JOB ABORTED")
        }

        if (snap) {
            if (snapshot()) println("SNAPSHOT DONE") else println("JOB ABORTED")
        }

        mode match {
            case "db2" =>
                action match {
                    case "read" =>
                        val df = readDB2(number)
                        if (df.isDefined) df.get.show else println("JOB ABORTED")

                    case "write" =>
                        if (writeDB2(number)) println("DONE") else println("JOB ABORTED")
                }

            case "cos" =>
                action match {
                    case "read" =>
                        val df = readCOS(number)
                        if (df.isDefined) df.get.show else println("JOB ABORTED")

                    case "write" =>
                        if (writeCOS(number)) println("DONE") else println("JOB ABORTED")
                }

            case "local" =>
                action match {
                    case "read" =>
                        val df = readLocal(number)
                        if (df.isDefined) df.get.show else println("JOB ABORTED")

                    case "write" =>
                        if (writeLocal(number)) println("DONE") else println("JOB ABORTED")
                }
        }
    }
}
