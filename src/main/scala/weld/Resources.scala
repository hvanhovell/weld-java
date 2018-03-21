package weld

import scala.collection.mutable
import scala.util.control.NonFatal

object Resources {
  def withCleanup[T](f: Resources => T): T = {
    val resources = new Resources
    try {
      val result = f(resources)
      resources.close()
      result
    } catch {
      case caught: Throwable =>
        // We want to throw and see the original exception so swallow exceptions during close.
        resources.close(swallowExceptions = true)
        throw caught
    }
  }
}

/**
 * Class that keeps track of resources.
 */
class Resources {
  private val closeables = mutable.Buffer.empty[AutoCloseable]

  /**
   * Add a closeable to the tracker.
   */
  def apply[T <: AutoCloseable](closeable: T): T = {
    closeables += closeable
    closeable
  }

  /**
   * Close all tracked resources. This function guarantees to call close on all tracked
   * resources, unless a fatal exception is thrown. If errors are encountered during the close
   * and `swallowExceptions` is set to `false`, the first [[Throwable]] will be stored and
   * others will be swallowed. If `swallowExceptions` is set to `true` all exceptions wil be
   * swallowed.
   */
  def close(swallowExceptions: Boolean = false): Unit = {
    var throwable: Throwable = null
    closeables.foreach { closeable =>
      try closeable.close() catch {
        case NonFatal(e) =>
          if (!swallowExceptions && throwable == null) {
            throwable = e
          }
      }
    }
    if (throwable != null) {
      throw throwable
    }
  }
}