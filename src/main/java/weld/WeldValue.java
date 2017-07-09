package weld;

import java.nio.ByteBuffer;

/**
 * This is a wrapper around the weld value object.
 *
 * Note that this object must be closed after usage.
 */
public class WeldValue implements AutoCloseable {
  final long handle;

  WeldValue(long handle, boolean dummy) {
    super();
    this.handle = handle;
  }

  public WeldValue() {
    this(ByteBuffer.allocateDirect(0));
  }

  public WeldValue(ByteBuffer buffer) {
    this(WeldJNI.weld_value_new(toDirect(buffer)));
  }

  /**
   * Create a weld value that pointing to a memory block allocated using
   * sun.misc.Unsafe or native code.
   */
  public WeldValue(long pointer) {
    this(WeldJNI.weld_value_new(pointer), false);
  }

  public void close() {
    WeldJNI.weld_value_free(this.handle);
  }

  public long getPointer() {
    return WeldJNI.weld_value_pointer(this.handle);
  }

  public long getRunId() {
    return WeldJNI.weld_value_run(this.handle);
  }

  private static ByteBuffer toDirect(final ByteBuffer input) {
    if (input.isDirect()) {
      return input;
    }
    final ByteBuffer direct = ByteBuffer.allocateDirect(input.remaining());
    return direct.put(input);
  }
}
