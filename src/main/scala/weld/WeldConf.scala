package weld

/**
 * A wrapper around the weld configuration object.
 *
 * Note that this object must be closed after usage.
 */
class WeldConf extends WeldManaged(WeldJNI.weld_conf_new()) {
  override protected def doClose(): Unit = {
    WeldJNI.weld_conf_free(handle)
  }

  override protected def cleaner = new WeldConf.Cleaner(handle)

  def get(key: String): String = {
    checkAccess()
    WeldJNI.weld_conf_get(handle, key)
  }

  def set(key: String, value: String): Unit = {
    checkAccess()
    WeldJNI.weld_conf_set(handle, key, value)
  }
}

object WeldConf {
  private[weld] class Cleaner(handle: Long) extends Runnable {
    override def run(): Unit = WeldJNI.weld_conf_free(handle)
  }
}
