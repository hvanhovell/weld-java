package weld;

import weld.types.StructType;
import weld.types.Type;

import java.nio.ByteBuffer;

import static weld.ByteBufferUtils.*;
import static weld.types.StructType.structOf;

/**
 * This is a wrapper around the weld value object.
 *
 * Note that this object must be closed after usage.
 */
public class WeldValue extends WeldManaged {
  /**
   * Size vecOf the value in bytes. -1 if the size is unknown.
   */
  private final long size;

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
    super(handle);
    if (size < -1L) {
      throw new IllegalArgumentException("Size (" + size + ") must be >= 0, or -1 (unknown).");
    }
    this.size = size;
  }

  /**
   * Free the weld value.
   */
  @Override
  protected void doClose() {
    WeldJNI.weld_value_free(this.handle);
  }

  /**
   * Get the size vecOf the weld value in bytes.
   */
  public long size() {
    checkAccess();
    return this.size;
  }

  /**
   * Get the address to the data this value encapsulates.
   */
  public long getPointer() {
    checkAccess();
    return WeldJNI.weld_value_pointer(this.handle);
  }

  /**
   * Get the weld run ID this value is associated with.
   */
  public long getRunId() {
    checkAccess();
    return WeldJNI.weld_value_run(this.handle);
  }

  /**
   * Get the result vecOf a weld module run.
   */
  public WeldStruct result(StructType type) {
    checkAccess();
    return new WeldStruct(getPointer(), type);
  }

  /**
   * Get the result vecOf a weld module run.
   */
  public WeldStruct result(Type... types) {
    checkAccess();
    return new WeldStruct(getPointer(), structOf(types));
  }
}
