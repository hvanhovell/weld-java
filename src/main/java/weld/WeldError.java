package weld;

/**
 * Wrapper for the native weld error object.
 *
 * Note that this object must be closed after usage.
 */
public class WeldError implements AutoCloseable {
  final long handle;

  public WeldError() {
    super();
    this.handle = WeldJNI.weld_error_new();
  }

  public void close() {
    WeldJNI.weld_error_free(this.handle);
  }

  public int getCode() {
    return WeldJNI.weld_error_code(this.handle);
  }

  public String getMessage() {
    return WeldJNI.weld_error_message(this.handle);
  }
}
