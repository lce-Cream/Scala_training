import org.scalatest.wordspec.AnyWordSpec

import connection.DB2Connection
import dataGeneration.DataFrameGenerator.testGenerate

val DB2Credentials: Map[String, String] = Map(
    "spark.db2.url"      -> "jdbc:db2://qwerty.databases.appdomain.cloud:30699/bludb:sslConnection=true;",
    "spark.db2.user"     -> "qwerty",
    "spark.db2.password" -> "qwerty",
    "spark.db2.table"    -> "ARSENI_SALES_TABLE2",
    "spark.db2.driver"   -> "com.ibm.db2.jcc.DB2Driver",
)

val connection = DB2Connection(DB2Credentials)
val testDF = testGenerate()

class DB2Test extends AnyWordSpec {

    "DB2Connection" should {
        "write data" in {
            assert(connection.write(testDF))
        }

        "read data" in {
            assert(connection.read() == testDF)
        }
    }
}
