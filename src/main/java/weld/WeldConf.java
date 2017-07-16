package weld;

/**
 * A wrapper around the weld configuration object.
 *
 * Note that this object must be closed after usage.
 */
public class WeldConf extends WeldManaged {

  public WeldConf() {
    super(WeldJNI.weld_conf_new());
  }

  @Override
  protected void doClose() {
    WeldJNI.weld_conf_free(this.handle);
  }

  public String get(final String key) {
    checkAccess();
    return WeldJNI.weld_conf_get(this.handle, key);
  }

  public void set(final String key, final String value) {
    checkAccess();
    WeldJNI.weld_conf_set(this.handle, key, value);
  }
}
