package weld

class WeldError extends WeldManaged(WeldJNI.weld_error_new()) {
  override protected def doClose(): Unit = {
    WeldJNI.weld_error_free(handle)
  }

  override protected def cleaner = new WeldError.Cleaner(handle)

  def code: Int = {
    checkAccess()
    WeldJNI.weld_error_code(handle)
  }

  def message: String = {
    checkAccess()
    WeldJNI.weld_error_message(handle)
  }
}

object WeldError {
  private[weld] class Cleaner(handle: Long) extends Runnable {
    override def run(): Unit = WeldJNI.weld_error_free(handle)
  }
}

class WeldException(val code: Int, message: String) extends RuntimeException(message) {
  def this(message: String) = this(-1, message)
  def this(error: WeldError, code: Option[String] = None) =
    this(error.code, error.message + code.map("\n" + _).getOrElse(""))
}
