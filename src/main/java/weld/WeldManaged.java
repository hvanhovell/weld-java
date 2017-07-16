package weld;

/**
 * Base class for classes for which an object exists that is managed by weld. All these classes
 * must be explicitly closed after use in order to prevent resource leaks.
 */
public abstract class WeldManaged implements AutoCloseable {
  /**
   * Handle to the native object.
   */
  final long handle;

  /**
   * Flag to indicate that the value is closed.
   */
  private boolean closed = false;

  /**
   * Create a new weld managed object.
   *
   * @param handle to the native object.
   */
  WeldManaged(long handle) {
    super();
    this.handle = handle;
  }

  /**
   * Close the weld managed object. Note that this method is idempotent.
   */
  @Override
  public final synchronized void close() {
    if (!closed) {
      doClose();
      closed = true;
    }
  }

  /**
   * Close the weld managed object.
   */
  protected abstract void doClose();

  /**
   * Check if the weld managed is closed.
   */
  public boolean closed() {
    return closed;
  }

  void checkAccess() {
    assert !closed : "Cannot access an already closed object";
  }
}
