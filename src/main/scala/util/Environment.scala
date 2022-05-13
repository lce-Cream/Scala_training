package util

object Environment {
    /** Returns mapping containing all spark settings found in env with underscores
     * replaced by dots in keys.
     *
     *  @return Map.
     */
    def getMap: Map[String, String] = {
        // take every env record which key starts with "spark"
        var config = sys.env.filter(tuple => tuple._1.startsWith("spark"))
        // replace '_' in key with '.'
        config.map(tuple => tuple._1.replace("_", ".") -> tuple._2)
        // it's done because Linux can't stand dots in env variables
        // and I have to use underscores and then convert it back to dots
    }
}
