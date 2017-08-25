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
   * Flag to indicate that the underlying reference will be cleaned up as soon as this object is
   * garbage collected.
   */
  private var autoClean = false

  /**
   * Close the weld managed object. Note that this method is idempotent.
   */
  override def close(): Unit = {
    if (!closed && !autoClean) {
      doClose()
      closed = true
    }
  }

  /**
   * Sets the closeable to auto cleaning mode. This means that the underlying handle is cleaned up
   * as soon as the object is garbage collected.
   */
  def markAutoCleanable(): Unit = {
    if (!closed && !autoClean) {
      autoClean = true
      Platform.registerForCleanUp(this, cleaner)
    }
  }

  /**
   * Close the weld managed object.
   */
  protected def doClose(): Unit

  /**
   * Create a cleaner for the managed object.
   */
  protected def cleaner: Runnable

  /**
   * Check if the weld managed is closed.
   */
  def isClosed(): Boolean = closed

  private[weld] def checkAccess() = {
    assert(!closed, "Cannot access an already closed object")
  }
}
