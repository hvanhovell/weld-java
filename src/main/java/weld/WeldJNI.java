package weld;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.scijava.nativelib.NativeLoader;

class WeldJNI {
  private WeldJNI() {
    super();
  }

  static {
    try {
      NativeLoader.loadLibrary("weld_java");
    } catch (IOException e) {
      // Fallback for testing
      System.loadLibrary("weld_java");
    }
  }

  static native long weld_value_new(long pointer);

  static native long weld_value_pointer(long handle);

  static native boolean weld_get_boolean(long handle, long offset);

  static native byte weld_get_byte(long handle, long offset);

  static native int weld_get_int(long handle, long offset);

  static native long weld_get_long(long handle, long offset);

  static native int weld_get_float(long handle, long offset);

  static native long weld_get_double(long handle, long offset);

  static native ByteBuffer weld_get_buffer(long handle, int size);

  static native long weld_get_buffer_pointer(ByteBuffer buffer);

  static native long weld_value_run(long handle);

  static native void weld_value_free(long handle);

  static native long weld_module_compile(String code, long conf, long error);

  static native long weld_module_run(long module, long conf, long input, long error);

  static native void weld_module_free(long module);

  static native long weld_error_new();

  static native void weld_error_free(long handle);

  static native int weld_error_code(long handle);

  static native String weld_error_message(long handle);

  static native long weld_conf_new();

  static native void weld_conf_free(long handle);

  static native String weld_conf_get(long handle, String key);

  static native void weld_conf_set(long handle, String key, String value);
}
