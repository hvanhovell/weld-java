package weld

/**
 * A wrapper around the weld configuration object.
 *
 * Note that this object must be closed after usage.
 */
class WeldConf extends WeldManaged(WeldJNI.weld_conf_new()) {
  override protected def doClose(): Unit = {
    WeldJNI.weld_conf_free(this.handle)
  }

  def get(key: String): String = {
    checkAccess()
    WeldJNI.weld_conf_get(this.handle, key)
  }

  def set(key: String, value: String): Unit = {
    checkAccess()
    WeldJNI.weld_conf_set(this.handle, key, value)
  }
}
