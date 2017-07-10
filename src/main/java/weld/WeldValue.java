package weld;

import java.nio.ByteBuffer;

import static weld.ByteBufferUtils.*;

/**
 * This is a wrapper around the weld value object.
 *
 * Note that this object must be closed after usage.
 */
public class WeldValue implements AutoCloseable {
  /**
   * Size of the value in bytes. -1 if the size is unknown.
   */
  private final long size;

  /**
   * Pointer to the native weld value.
   */
  final long handle;

  /**
   * Create a empty weld value.
   */
  public WeldValue() {
    this(allocateDirect(0));
  }

  /**
   * Create a weld value from a byte buffer.
   */
  public WeldValue(ByteBuffer buffer) {
    this(WeldJNI.weld_get_buffer_pointer(toDirect(buffer)), buffer.limit());
  }

  /**
   * Create a weld value that is pointing to a memory block allocated using
   * sun.misc.Unsafe or native code.
   */
  public WeldValue(long pointer) {
    this(pointer, -1L);
  }

  /**
   * Create a weld value that pointing to a memory block allocated using
   * sun.misc.Unsafe or native code.
   */
  public WeldValue(long pointer, long size) {
    this(WeldJNI.weld_value_new(pointer), size, false);
  }

  WeldValue(long handle, long size, boolean dummy) {
    super();
    if (size < -1L) {
      throw new IllegalArgumentException("Size (" + size + ") must be >= 0, or -1 (unknown).");
    }
    this.handle = handle;
    this.size = size;
  }

  /**
   * Free the weld value.
   */
  public void close() {
    WeldJNI.weld_value_free(this.handle);
  }

  /**
   * Get the size of the weld value in bytes.
   */
  public long size() {
    return this.size;
  }

  /**
   * Get the pointer to the data this value encapsulates.
   */
  public long getPointer() {
    return WeldJNI.weld_value_pointer(this.handle);
  }

  /**
   * Get a byte buffer pointing to the data this value encapsulates.
   */
  public ByteBuffer getBuffer() {
    checkSized();
    if (this.size > Integer.MAX_VALUE) {
      throw new WeldException("Cannot create ByteBuffer from a WeldValue that is larger than Int.Max.");
    }
    return WeldJNI.weld_get_buffer(getPointer(), (int) this.size);
  }

  /**
   * Get the weld run ID this value is associated with.
   */
  public long getRunId() {
    return WeldJNI.weld_value_run(this.handle);
  }

  /**
   * Check if the value has a valid size.
   */
  private void checkSized() {
    if (size < 0) {
      throw new IllegalArgumentException("Value has no valid size.");
    }
  }

  /**
   * Create a sized weld value. This is only allowed when the value has no size yet.
   */
  public WeldValue size(long size) {
    if (this.size >= 0) {
      throw new IllegalStateException("Size has already been set");
    }
    if (size < 0) {
      throw new IllegalArgumentException("Size (" + size + ") must be >= 0.");
    }
    return new WeldValue(handle, size);
  }

  /**
   * Convert the raw `WeldValue` to a 'struct'. This wraps the pointer managed by the weld value.
   * Note that the result of a module run points to a struct, this means that you need to call
   * `struct.getStruct(0, ..)` to work with the actual result, or use the `result(..)` method
   * instead.
   */
  public WeldStruct struct() {
    checkSized();
    return new WeldStruct(getPointer(), size, null);
  }

  /**
   * Get the result of a weld module run.
   */
  public WeldStruct result(int resultSize) {
    // TODO(hvanhovell) figure out why I cannot set this in the module. The JVM crashes Every time
    // I try to do that.
    return size(8).struct().getStruct(0, resultSize);
  }
}
