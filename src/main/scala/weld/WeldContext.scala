package weld

class WeldContext private(handle: Long) extends WeldManaged(handle) {
  override protected def doClose(): Unit = {
    WeldJNI.weld_context_free(handle)
  }

  /**
   * Obtain the memory usage of this context.
   */
  def memory_usage(): Long = {
    checkAccess()
    WeldJNI.weld_context_memory_usage(handle)
  }

  /**
   * Create a cleaner for the managed object.
   */
  override protected def cleaner = new WeldContext.Cleaner(handle)
}

object WeldContext {
  /**
   * Initialize a [[WeldContext]] using the default configuration.
   */
  def init(): WeldContext = {
    val conf = new WeldConf
    try init(conf) finally {
      conf.close()
    }
  }

  /**
   * Initialize a new a [[WeldContext]].
   */
  def init(conf: WeldConf): WeldContext = {
    val context = new WeldContext(WeldJNI.weld_context_new(conf.handle))
    // Returns NULL upon error.
    if (context.handle == 0) {
      val e = new WeldException("WeldContext initialization failed")
      throw e
    }
    context
  }

  private[weld] class Cleaner(handle: Long) extends Runnable {
    override def run(): Unit = WeldJNI.weld_context_free(handle)
  }
}
