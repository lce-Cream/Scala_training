package util
import org.apache.commons.cli._

object CLIParser {
    val options = new Options()
    val parser = new BasicParser()
    val formatter = new HelpFormatter()

    options.addOption("", true, "url for db2 instance")

    val db2Url = OptionBuilder.withLongOpt("Db2Url").withArgName("url").hasArg().withDescription("desc").create()

    options.addOption("spark.db2.user", true, "username for db2 instance")
    options.addOption("spark.db2.password", true, "password for db2 instance")
    options.addOption("spark.cos.access.key", true, "cos access key")
    options.addOption("spark.cos.secret.key", true, "cos secret key")
    options.addOption("spark.cos.endpoint", true, "cos http endpoint")
    options.addOption("spark.mysql.url", true, "url for mysql instance")
    options.addOption("spark.mysql.user", true, "username for mysql instance")
    options.addOption("spark.mysql.password", true, "password for mysql instance")
    options.addOption("spark.mysql.table", true, "mysql table")
    options.addOption("spark.local.path", true, "local path to a directory")

    def parse(args: Array[String]): Map[String, String] = {
        var config = Map[String, String]()

        try {
            parser.parse(options, args)
        }
        catch {
            case e: Exception => {
                println(e.getMessage)
                formatter.printHelp("ant", options)
            }
        }

        val line = parser.parse(options, args)

        line.getOptions.foreach(option => {
            println(option.getArgName + option.getValue)
            config += {(option.getArgName, option.getValue)}
        })
        config
    }
}
