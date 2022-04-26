package util

import util.DefaultConfig.{DB2Credentials, COSCredentials, MySQLCredentials, LocalCredentials}

object Environment {
    /** Returns mapping containing all settings found in env with names
     * matching config keys.
     *
     *  @return DataFrame.
     */
    def getMap: Map[String, String] = {
        val parameters = DB2Credentials++COSCredentials++MySQLCredentials++LocalCredentials
        val config = sys.env.filter(tuple => parameters.contains(tuple._1))
        config
    }
}
