package util
// this library is complete garbage, but others are even worse
import org.apache.commons.cli._
import scala.util.Try

object CLIParser {
    val options = new Options()
    val parser = new BasicParser()
    val formatter = new HelpFormatter()

    // first option group is used for basic operations

    var mode = new Option("m", "mode", true, "Launch process using db2/cos/local.")
    mode.setRequired(false)
    mode.setArgName("str")

    val action = new Option("a", "action", true, "Choose to read/write data.")
    action.setRequired(false)
    action.setArgName("str")

    val number = new Option("n", "number", true, "Amount of records to use.")
    number.setRequired(false)
    number.setArgName("int")

    val help = new Option("h", "help", false, "Show help massage.")
    help.setRequired(false)

    val verbose = new Option("v", "verbose", false, "Print debug info during execution.")
    verbose.setRequired(false)

    // second option group provides functionality for usage in DAGs

    val calculate = new Option("c", "calc", false, "calculate annual sales and overwrite current table")
    calculate.setRequired(false)

    val snapshot = new Option("s", "snap", false, "snapshot table to cos")
    snapshot.setRequired(false)

    options.addOption(mode)
    options.addOption(action)
    options.addOption(number)
    options.addOption(help)
    options.addOption(verbose)
    options.addOption(calculate)
    options.addOption(snapshot)

    /** Parses CLI arguments into mapping.
     *
     *  @param args CommandLine class instance with parsed arguments.
     *  @return true if arguments are correct, false otherwise.
     */
    def checkArguments(args: CommandLine): Boolean = {
        val mods = List("db2", "cos", "local")
        val actions = List("read", "write")

        val mode   = args.getOptionValue("mode")
        val action = args.getOptionValue("action")
        val number = args.getOptionValue("number")

        val isValidGroup1 = Try(
              mods.contains(mode)
              && actions.contains(action)
              && number.toInt.isInstanceOf[Int]
        ).getOrElse(false)

        val calculate = args.hasOption("calc")
        val snapshot = args.hasOption("snap")

        val isValidGroup2 = calculate || snapshot

        // return true if either of argument groups is valid but not both
        isValidGroup1 != isValidGroup2
    }

    /** Parses CLI arguments into mapping.
     *
     *  @param args array of command line arguments.
     *  @return Map[String, String].
     */
    def parse(args: Array[String]): Map[String, String] = {
        if (args.contains("-h") || args.contains("--help")) {
            formatter.printHelp("Configure job to run data load and data transform processes", options)
            sys.exit(0)
        }

        if (!checkArguments(parser.parse(options, args))) throw new Exception("Incorrect arguments")
        var config = Map[String, String]()
        val line = parser.parse(options, args)

        line.getOptions.foreach(option => {
            config += {(option.getLongOpt, option.getValue)}
        })
        config
    }
}
