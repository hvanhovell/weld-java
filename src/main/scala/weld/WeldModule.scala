package weld

class WeldModule private(handle: Long) extends WeldManaged(handle) {
  override protected def doClose(): Unit = {
    WeldJNI.weld_module_free(handle)
  }

  /**
   * Run the module with default parameters.
   */
  def run(input: WeldValue): WeldValue = {
    val conf = new WeldConf
    try run(conf, input) finally {
      conf.close()
    }
  }

  /**
   * Run the module.
   */
  def run(conf: WeldConf, input: WeldValue): WeldValue = {
    checkAccess()
    val error = new WeldError
    val output = new WeldValue(
      WeldJNI.weld_module_run(
        handle,
        conf.handle,
        input.handle,
        error.handle),
      size = -1L)
    if (error.code != 0) {
      val e = new WeldException(error)
      error.close()
      output.close()
      throw e
    }
    output
  }

  /**
   * Create a cleaner for the managed object.
   */
  override protected def cleaner = new WeldModule.Cleaner(handle)
}

object WeldModule {
  /**
   * Compile the given code into a [[WeldModule]] using the default configuration.
   */
  def compile(code: String): WeldModule = {
    val conf = new WeldConf
    try compile(conf, code) finally {
      conf.close()
    }
  }

  /**
   * Compile the given code into a [[WeldModule]].
   */
  def compile(conf: WeldConf, code: String): WeldModule = {
    val error = new WeldError
    val module = new WeldModule(WeldJNI.weld_module_compile(code, conf.handle, error.handle))
    if (error.code != 0) {
      val e = new WeldException(error, Some(code))
      error.close()
      module.close()
      throw e
    }
    module
  }

  private[weld] class Cleaner(handle: Long) extends Runnable {
    override def run(): Unit = WeldJNI.weld_module_free(handle)
  }
}
