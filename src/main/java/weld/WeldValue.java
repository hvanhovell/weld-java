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
   * Get the address to the data this value encapsulates.
   */
  public long getPointer() {
    return WeldJNI.weld_value_pointer(this.handle);
  }

  /**
   * Get the weld run ID this value is associated with.
   */
  public long getRunId() {
    return WeldJNI.weld_value_run(this.handle);
  }

  /**
   * Convert the raw `WeldValue` to a 'struct'.
   */
  public WeldStruct struct() {
    if (size < 0) {
      throw new IllegalArgumentException("Value has no valid size.");
    }
    return new WeldStruct(getPointer(), size);
  }

  /**
   * Get the result of a weld module run.
   */
  public WeldStruct result(int size) {
    // The result of a module run, is a address to a struct address; so we need to dereference it.
    return new WeldStruct(getPointer(), size);
  }
}
