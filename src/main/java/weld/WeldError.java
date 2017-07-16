package weld;

/**
 * Wrapper for the native weld error object.
 *
 * Note that this object must be closed after usage.
 */
public class WeldError extends WeldManaged {

  public WeldError() {
    super(WeldJNI.weld_error_new());
  }

  @Override
  protected void doClose() {
    WeldJNI.weld_error_free(this.handle);
  }

  public int getCode() {
    checkAccess();
    return WeldJNI.weld_error_code(this.handle);
  }

  public String getMessage() {
    checkAccess();
    return WeldJNI.weld_error_message(this.handle);
  }
}
