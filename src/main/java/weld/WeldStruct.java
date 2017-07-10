package weld;

import java.nio.ByteBuffer;

/**
 * Wrapper for a weld struct value.
 */
public class WeldStruct extends WeldObject {
  /**
   * Create a weld struct that pointing to a memory block allocated using
   * sun.misc.Unsafe or native code.
   */
  WeldStruct(long pointer, long size, ByteBuffer ref) {
    super(pointer, size, ref);
  }

  /**
   * Create a struct from a byte buffer.
   */
  public static WeldStruct struct(ByteBuffer buffer) {
    final ByteBuffer direct = ByteBufferUtils.toDirect(buffer);
    return new WeldStruct(WeldJNI.weld_get_buffer_pointer(direct), direct.limit(), direct);
  }

  /**
   * Convert the struct into a WeldValue.
   */
  public WeldValue toValue() {
    return new WeldValue(pointer, size);
  }

  /**
   * Create a struct from an argument array.
   */
  public static WeldStruct struct(final Object... values) {
    // Determine the size.
    int size = 0;
    for (int i = 0; i < values.length; i++) {
      final Object value = values[i];
      if (value instanceof Boolean || value instanceof Byte) {
        size += 1;
      } else if (value instanceof Integer || value instanceof Float) {
        size += 4;
      } else if (value instanceof Long || value instanceof Double || value instanceof WeldStruct) {
        size += 8;
      } else if (value instanceof WeldVec) {
        size += 16;
      } else {
        throw new IllegalArgumentException("Unsupported struct value[" + i + "]: " + value);
      }
    }

    // Create the struct
    final ByteBuffer buffer = ByteBufferUtils.allocateDirect(size);
    for (Object value : values) {
      if (value instanceof Boolean) {
        final boolean condition = (Boolean) value;
        buffer.put(condition ? (byte) 1 : (byte) 0);
      } else if (value instanceof Byte) {
        buffer.put((Byte) value);
      } else if (value instanceof Integer) {
        buffer.putInt((Integer) value);
      } else if (value instanceof Float) {
        buffer.putFloat((Float) value);
      } else if (value instanceof Long) {
        buffer.putLong((Long) value);
      } else if (value instanceof Double) {
        buffer.putDouble((Double) value);
      } else if (value instanceof WeldStruct) {
        buffer.putLong(((WeldStruct) value).pointer);
      } else if (value instanceof WeldVec) {
        final WeldVec vec = (WeldVec) value;
        buffer.putLong(vec.pointer);
        buffer.putLong(vec.size);
      }
    }
    buffer.flip();
    return struct(buffer);
  }
}
