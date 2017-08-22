package weld

import java.nio.ByteBuffer

import org.scijava.nativelib.NativeLibraryUtil

object WeldJNI {
  if (!NativeLibraryUtil.loadNativeLibrary(getClass, "weld_java")) {
    // Fallback for testing
    System.loadLibrary("weld_java")
  }

  @native
  private[weld] def weld_value_new(pointer: Long): Long

  @native
  private[weld] def weld_value_pointer(handle: Long): Long

  @native
  private[weld] def weld_get_buffer_pointer(buffer: ByteBuffer): Long

  @native
  private[weld] def weld_value_run(handle: Long): Long

  @native
  private[weld] def weld_value_memory_usage(handle: Long): Long

  @native
  private[weld] def weld_value_free(handle: Long): Unit

  @native
  private[weld] def weld_module_compile(code: String, conf: Long, error: Long): Long

  @native
  private[weld] def weld_module_run(module: Long, conf: Long, input: Long, error: Long): Long

  @native
  private[weld] def weld_module_free(module: Long): Unit

  @native
  private[weld] def weld_error_new(): Long

  @native
  private[weld] def weld_error_free(handle: Long): Unit

  @native
  private[weld] def weld_error_code(handle: Long): Int

  @native
  private[weld] def weld_error_message(handle: Long): String

  @native
  private[weld] def weld_conf_new(): Long

  @native
  private[weld] def weld_conf_free(handle: Long): Unit

  @native
  private[weld] def weld_conf_get(handle: Long, key: String): String

  @native
  private[weld] def weld_conf_set(handle: Long, key: String, value: String): Unit

  @native
  private[weld] def weld_load_library(filename: String, error: Long): Unit

  @native
  private[weld] def weld_set_log_level(loglevel: String): Unit
}
