package weld;

import java.nio.ByteBuffer;

/**
 * Wrapper for a weld vec value.
 */
public class WeldVec extends WeldObject {
  private final int elementSize;

  /**
   * Create a weld struct that pointing to a memory block allocated using
   * sun.misc.Unsafe or native code.
   */
  WeldVec(long pointer, long size, int elementSize, ByteBuffer ref) {
    super(pointer, size, ref);
    if (elementSize <= 0) {
      throw new IllegalArgumentException("ElementSize(" + elementSize + ") must be >= 0.");
    }
    if (size % elementSize != 0) {
      throw new IllegalArgumentException(
              "Size(" + size + ") must be a multiple of the elementSize(" + elementSize + ")");
    }
    this.elementSize = elementSize;
  }

  /**
   * Get the size of single element in the vector.
   */
  public int elementSize() {
    return this.elementSize;
  }

  /**
   * Get the number of elements in the vector.
   */
  public long numElements() {
    return this.size / this.elementSize;
  }

  /**
   * Create a vector from a byte buffer.
   */
  public static WeldVec vec(ByteBuffer buffer, int elementSize) {
    final ByteBuffer direct = ByteBufferUtils.toDirect(buffer);
    return new WeldVec(WeldJNI.weld_get_buffer_pointer(direct), direct.limit(), elementSize, direct);
  }

  /**
   * Create a vector from a boolean array.
   */
  public static WeldVec vec(final boolean... values) {
    final ByteBuffer direct = ByteBufferUtils.allocateDirect(values.length);
    for (final boolean value : values) {
      direct.put(value ? (byte) 1 : (byte) 0);
    }
    direct.flip();
    return vec(direct, 1);
  }

  /**
   * Create a vector from a string.
   */
  public static WeldVec vec(final String text) {
    return vec(text.getBytes());
  }

  /**
   * Create a vector from a byte array.
   */
  public static WeldVec vec(final byte... values) {
    final ByteBuffer direct = ByteBufferUtils.allocateDirect(values.length);
    direct.put(values).flip();
    return vec(direct, 1);
  }

  /**
   * Create a vector from a int array.
   */
  public static WeldVec vec(final int... values) {
    final ByteBuffer direct = ByteBufferUtils.allocateDirect(values.length * 4);
    direct.asIntBuffer().put(values).flip();
    return vec(direct, 4);
  }

  /**
   * Create a vector from a float array.
   */
  public static WeldVec vec(final float... values) {
    final ByteBuffer direct = ByteBufferUtils.allocateDirect(values.length * 4);
    direct.asFloatBuffer().put(values).flip();
    return vec(direct, 4);
  }

  /**
   * Create a vector from a long array.
   */
  public static WeldVec vec(final long... values) {
    final ByteBuffer direct = ByteBufferUtils.allocateDirect(values.length * 8);
    direct.asLongBuffer().put(values).flip();
    return vec(direct, 8);
  }

  /**
   * Create a vector from a double array.
   */
  public static WeldVec vec(final double... values) {
    final ByteBuffer direct = ByteBufferUtils.allocateDirect(values.length * 8);
    direct.asDoubleBuffer().put(values).flip();
    return vec(direct, 8);
  }

  /**
   * Create a vector from a struct array.
   */
  public static WeldVec vec(final WeldStruct... values) {
    final ByteBuffer direct = ByteBufferUtils.allocateDirect(values.length * 8);
    for (final WeldStruct value : values) {
      direct.putLong(value.pointer);
    }
    direct.flip();
    return vec(direct, 8);
  }

  /**
   * Create a vector from a vec array.
   */
  public static WeldVec vec(final WeldVec... values) {
    final ByteBuffer direct = ByteBufferUtils.allocateDirect(values.length * 16);
    for (final WeldVec value : values) {
      direct.putLong(value.pointer);
      direct.putLong(value.size);
    }
    direct.flip();
    return vec(direct, 16);
  }
}
