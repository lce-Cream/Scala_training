package util
import org.apache.commons.cli._

object CLIParser {
    val options = new Options()
    val parser = new BasicParser()
    val formatter = new HelpFormatter()
    val a = new OptionGroup()

    options.addOption("dur", "spark.db2.url", true, "url for db2 instance")
    options.addOption("dus", "spark.db2.user", true, "username for db2 instance")
    options.addOption("dps", "spark.db2.password", true, "password for db2 instance")
    options.addOption("cak", "spark.cos.access.key", true, "cos access key")
    options.addOption("csk", "spark.cos.secret.key", true, "cos secret key")
    options.addOption("cep", "spark.cos.endpoint", true, "cos http endpoint")
    options.addOption("qlur", "spark.mysql.url", true, "url for mysql instance")
    options.addOption("qlus", "spark.mysql.user", true, "username for mysql instance")
    options.addOption("qlps", "spark.mysql.password", true, "password for mysql instance")
    options.addOption("qltb", "spark.mysql.table", true, "mysql table")
    options.addOption("path", "spark.local.path", true, "local path to a directory")

    /** Parses CLI arguments into mapping.
     *
     *  @param args array of command line arguments.
     *  @return Map[String, String].
     */
    def parse(args: Array[String]): Map[String, String] = {
        try {
            parser.parse(options, args)
        }
        catch {
            case e: Exception => {
                println(e.getMessage)
                formatter.printHelp("provide spark configuration", options)
                sys.exit(1)
            }
        }

        var config = Map[String, String]()
        val line = parser.parse(options, args)
        line.getOptions.foreach(option => {
            config += {(option.getLongOpt, option.getValue)}
        })
        config
    }
}
