import org.scalatest.wordspec.AnyWordSpec
import util.CLIParser

class CLITest extends AnyWordSpec{
    "CLI" should {
        "return map with parsed arguments" in {
            assert(
                CLIParser.parse(Array("-m","db2","-a","read","-n","10"))
                  == Map[String, String](
                    "mode"   -> "db2",
                    "action" -> "read",
                    "number" -> "10",
                )
            )
            assert(
                CLIParser.parse(Array("--mode","cos","--action","write","--number","100"))
                  == Map[String, String](
                    "mode"   -> "cos",
                    "action" -> "write",
                    "number" -> "100",
                )
            )
            assert(
                CLIParser.parse(Array("--number","5","-a","write","--mode","local"))
                  == Map[String, String](
                    "mode"   -> "local",
                    "action" -> "write",
                    "number" -> "5",
                )
            )
        }

        "throw exception when number of arguments is not equal to 3" in {
            assertThrows[Exception](CLIParser.parse(Array("-m","db2","-a","read")))
            assertThrows[Exception](CLIParser.parse(Array("-m","db2","-a","read","-n","-k","arg")))
            assertThrows[Exception](CLIParser.parse(Array()))
        }

        "throw exception if argument's value is incorrect" in {
            assertThrows[Exception](CLIParser.parse(Array("-m","db2","-a","read","-n","ten")))
            assertThrows[Exception](CLIParser.parse(Array("-m","db2","-a","delete","-n","10")))
            assertThrows[Exception](CLIParser.parse(Array("-m","postgres","-a","write","-n","10")))
        }
    }
}
