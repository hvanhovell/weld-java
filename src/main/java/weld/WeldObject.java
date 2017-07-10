package weld;

import java.nio.ByteBuffer;

/**
 * Base class for complex weld values.
 */
public abstract class WeldObject {
  /**
   * Pointer to the underlying data.
   */
  final long pointer;

  /**
   * Size of the object in bytes.
   */
  final long size;

  /**
   * Reference to the created byte buffer, to prevent the direct buffer from being garbage
   * collected.
   */
  private final ByteBuffer ref;

  /**
   * Create a weld object that points to a memory block allocated using
   * sun.misc.Unsafe or native code.
   */
  WeldObject(long pointer, long size, ByteBuffer ref) {
    super();
    if (size < 0) {
      throw new IllegalArgumentException();
    }
    this.pointer = pointer;
    this.size = size;
    this.ref = ref;
  }

  /**
   * Check if the memory access is valid.
   */
  private void assertValidAccess(int offset, int size) {
    assert size >= 0: "size (" + size + ") should be >= 0";
    assert offset >= 0 : "offset (" + offset + ") should be >= 0";
    assert offset + size <= this.size : "size + offset (" + (offset + size) + ") should be <= " + this.size;
  }

  /**
   * Get the size of object in bytes.
   */
  public long size() {
    return size;
  }

  public boolean getBoolean(int offset) {
    assertValidAccess(offset, 1);
    return WeldJNI.weld_get_boolean(this.pointer, offset);
  }

  public byte getByte(int offset) {
    assertValidAccess(offset, 1);
    return WeldJNI.weld_get_byte(this.pointer, offset);
  }

  public int getInt(int offset) {
    assertValidAccess(offset, 4);
    return WeldJNI.weld_get_int(this.pointer, offset);
  }

  public long getLong(int offset) {
    assertValidAccess(offset, 8);
    return WeldJNI.weld_get_long(this.pointer, offset);
  }

  public float getFloat(int offset) {
    assertValidAccess(offset, 4);
    return WeldJNI.weld_get_float(this.pointer, offset);
  }

  public double getDouble(int offset) {
    assertValidAccess(offset, 8);
    return WeldJNI.weld_get_double(this.pointer, offset);
  }

  public WeldVec getVec(int offset, int elementSize) {
    assertValidAccess(offset, 16);
    long pointer = WeldJNI.weld_get_long(this.pointer, offset);
    long size = WeldJNI.weld_get_long(this.pointer, offset + 8) * elementSize;
    return new WeldVec(pointer, size, elementSize, null);
  }

  public WeldStruct getStruct(int offset, long size) {
    assertValidAccess(offset, 8);
    long pointer = WeldJNI.weld_get_long(this.pointer, offset);
    return new WeldStruct(pointer, size, null);
  }
}
