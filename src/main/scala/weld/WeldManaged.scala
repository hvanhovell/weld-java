package weld

/**
 * Base class for classes for which an object exists that is managed by weld. All these classes
 * must be explicitly closed after use in order to prevent resource leaks.
 */
abstract class WeldManaged(val handle: Long) extends AutoCloseable {
  /**
   * Flag to indicate that the value is closed.
   */
  private var closed = false

  /**
   * Close the weld managed object. Note that this method is idempotent.
   */
  override def close(): Unit = {
    if (!closed) {
      doClose()
      closed = true
    }
  }

  /**
   * Close the weld managed object.
   */
  protected def doClose(): Unit

  /**
   * Check if the weld managed is closed.
   */
  def isClosed(): Boolean = closed

  private[weld] def checkAccess() = {
    assert(!closed, "Cannot access an already closed object")
  }
}
