package weld

object Weld {
  def loadLibrary(filename: String): Unit = {
    val error = new WeldError
    WeldJNI.weld_load_library(filename, error.handle)
    if (error.code != 0) {
      val e = new WeldException(error)
      error.close()
      throw e
    }
  }

  def setLogLevel(level: String): Unit = WeldJNI.weld_set_log_level(level)
}
