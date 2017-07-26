package weld

class WeldError extends WeldManaged(WeldJNI.weld_error_new()) {
  override protected def doClose(): Unit = {
    WeldJNI.weld_error_free(this.handle)
  }

  def code: Int = {
    checkAccess()
    WeldJNI.weld_error_code(this.handle)
  }

  def message: String = {
    checkAccess()
    WeldJNI.weld_error_message(this.handle)
  }
}

class WeldException(val code: Int, message: String) extends RuntimeException(message) {
  def this(message: String) = this(-1, message)
  def this(error: WeldError) = this(error.code, error.message)
}