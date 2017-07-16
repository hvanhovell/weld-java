package weld;

/**
 * Wrapper for a weld struct value.
 */
public final class WeldStruct extends WeldObject implements AutoCloseable {
  /**
   * Flag to indicate that we should free the object when close() is called
   */
  private boolean freeOnClose;

  /**
   * Create a weld struct that points to a memory address.
   */
  public WeldStruct(long pointer, long size) {
    this(pointer, size, false);
  }

  /**
   * Create a weld struct that points to a memory address and that may optionally be freed when
   * close() is called.
   */
  private WeldStruct(long pointer, long size, boolean freeOnClose) {
    super(pointer, size);
    this.freeOnClose = freeOnClose;
  }

  /**
   * Close the weld struct, it should not be used after this call as it will try free the
   * memory location it points to.
   */
  @Override
  public synchronized void close() {
    if (freeOnClose) {
      Platform.freeMemory(address());
      freeOnClose = false;
    }
  }

  /**
   * Convert the struct into a WeldValue.
   */
  public WeldValue toValue() {
    return new WeldValue(address(), size());
  }

  /**
   * Create a struct from an argument array. Note that this builds an aligned struct, this means
   * that a value will always be written to an offset that aligns with its size.
   */
  public static WeldStruct struct(final Object... values) {
    // Determine the size.
    // Note that we need to align the offset to the size of the value of we are about to write.
    long headerSize = 0;
    long vectorSize = 0;
    for (int i = 0; i < values.length; i++) {
      final Object value = values[i];
      if (value instanceof Boolean || value instanceof Byte) {
        headerSize += 1;
      } else if (value instanceof Integer || value instanceof Float) {
        headerSize = ceil4(headerSize) + 4;
      } else if (value instanceof Long || value instanceof Double || value instanceof WeldVec) {
        headerSize = ceil8(headerSize) + 8;
      } else if (value instanceof WeldVec.Builder) {
        headerSize = ceil8(headerSize) + 16;
        vectorSize += ceil8(((WeldVec.Builder) value).size());
      } else {
        throw new IllegalArgumentException("Unsupported struct value[" + i + "]: " + value);
      }
    }
    headerSize = ceil8(headerSize);

    // Create the struct
    final long address = Platform.allocateMemory(headerSize + vectorSize);
    long headerOffset = 0;
    long dataOffset = headerSize;
    for (Object value : values) {
      if (value instanceof Boolean) {
        final boolean condition = (Boolean) value;
        Platform.putByte(address + headerOffset, condition ? (byte) 1 : (byte) 0);
        headerOffset += 1;
      } else if (value instanceof Byte) {
        Platform.putByte(address + headerOffset, (Byte) value);
        headerOffset += 1;
      } else if (value instanceof Integer) {
        headerOffset = ceil4(headerOffset);
        Platform.putInt(address + headerOffset, (Integer) value);
        headerOffset += 4;
      } else if (value instanceof Float) {
        headerOffset = ceil4(headerOffset);
        Platform.putFloat(address + headerOffset, (Float) value);
        headerOffset += 4;
      } else if (value instanceof Long) {
        headerOffset = ceil8(headerOffset);
        Platform.putLong(address + headerOffset, (Long) value);
        headerOffset += 8;
      } else if (value instanceof Double) {
        headerOffset = ceil8(headerOffset);
        Platform.putDouble(address + headerOffset, (Double) value);
        headerOffset += 8;
      } else if (value instanceof WeldVec) {
        final WeldVec vec = (WeldVec) value;
        headerOffset = ceil8(headerOffset);
        Platform.putLong(address + headerOffset, vec.address());
        Platform.putLong(address + headerOffset + 8, vec.numElements());
        headerOffset += 16;
      } else if (value instanceof WeldVec.Builder) {
        final WeldVec.Builder builder = (WeldVec.Builder) value;
        headerOffset = ceil8(headerOffset);
        Platform.putLong(address + headerOffset, address + dataOffset);
        Platform.putLong(address + headerOffset + 8, builder.numElements);
        builder.putValues(address + dataOffset);
        headerOffset += 16;
        dataOffset += ceil8(builder.size());
      }
    }
    final WeldStruct result = new WeldStruct(address, headerSize, true);
    Platform.registerForCleanUp(result);
    return result;
  }

  /**
   * Round a long up to its nearest multiple of 4.
   */
  private static long ceil4(long value) {
    return (value + 0x3L) & ~0x3L;
  }

  /**
   * Round an int up to its nearest multiple of 8.
   */
  private static long ceil8(long value) {
    return (value + 0x7L) & ~0x7L;
  }
}
