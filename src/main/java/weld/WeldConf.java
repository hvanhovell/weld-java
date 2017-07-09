package weld;

/**
 * A wrapper around the weld configuration object.
 *
 * Note that this object must be closed after usage.
 */
public class WeldConf implements AutoCloseable {
  final long handle;

  public WeldConf() {
    super();
    this.handle = WeldJNI.weld_conf_new();
  }

  public void close() {
    WeldJNI.weld_conf_free(this.handle);
  }

  public String get(final String key) {
    return WeldJNI.weld_conf_get(this.handle, key);
  }

  public void set(final String key, final String value) {
    WeldJNI.weld_conf_set(this.handle, key, value);
  }
}
