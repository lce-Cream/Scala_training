package util

object Util {
    /** Calculates amout of seconds elapsed from passed time
     *
     *  @param start amount DataFrame's rows
     *  @return String.
     */
    def getDuration(start: Long): String = {
        s"${(System.currentTimeMillis - start) / 1000.0}s"
    }
}
