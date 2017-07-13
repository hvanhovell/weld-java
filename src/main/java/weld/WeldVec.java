package weld;

/**
 * Wrapper for a weld vec value.
 */
public final class WeldVec extends WeldObject {
  /**
   * Size of the individual element.
   */
  private final int elementSize;

  /**
   * Create a weld vec that points to a memory block allocated using
   * sun.misc.Unsafe or native code.
   */
  public WeldVec(long pointer, long size, int elementSize) {
    super(pointer, size);
    if (elementSize <= 0) {
      throw new IllegalArgumentException("ElementSize(" + elementSize + ") must be > 0.");
    }
    if (size % elementSize != 0) {
      throw new IllegalArgumentException("Size(" + size + ") must be a multiple of the elementSize(" + elementSize + ")");
    }
    this.elementSize = elementSize;
  }

  @Override
  long offsetToAddress(int offset, int bytesToRead) {
    assert bytesToRead == elementSize() : "BytesToRead(" + bytesToRead + ") must match element size: " + elementSize();
    assert offset % elementSize() == 0 : "Offset(" + offset + ") must be aligned to the element size: " + elementSize();
    return super.offsetToAddress(offset, bytesToRead);
  }

  /**
   * Get the size of single element in the vector.
   */
  public int elementSize() {
    return elementSize;
  }

  /**
   * Get the number of elements in the vector.
   */
  public long numElements() {
    return size() / elementSize;
  }

  /**
   * Get a struct from the vector at the given offset.
   */
  public WeldStruct getStruct(int offset) {
    return new WeldStruct(offsetToAddress(offset, elementSize), elementSize);
  }

  /**
   * Create a vector from a memory location.
   */
  public static WeldVec fromAddress(long pointer, long size, int elementSize) {
    return new WeldVec(pointer, size, elementSize);
  }

  /**
   * Create a vector from a boolean array.
   */
  public static Builder vec(final boolean... values) {
    return new Builder(values.length, 1) {
      @Override
      void putValues(long address) {
        for (int i = 0; i < values.length; i++) {
          Platform.putByte(address + i, values[i] ? (byte) 1 : (byte) 0);
        }
      }
    };
  }

  /**
   * Create a vector from a string.
   */
  public static Builder vec(final String text) {
    return vec(text.getBytes());
  }

  /**
   * Create a vector from a byte array.
   */
  public static Builder vec(final byte... values) {
    return new Builder(values.length, 1) {
      @Override
      void putValues(long address) {
        Platform.putBytes(address, values);
      }
    };
  }

  /**
   * Create a vector from a int array.
   */
  public static Builder vec(final int... values) {
    return new Builder(values.length, 4) {
      @Override
      void putValues(long address) {
        Platform.putInts(address, values);
      }
    };
  }

  /**
   * Create a vector from a float array.
   */
  public static Builder vec(final float... values) {
    return new Builder(values.length, 4) {
      @Override
      void putValues(long address) {
        Platform.putFloats(address, values);
      }
    };
  }

  /**
   * Create a vector from a long array.
   */
  public static Builder vec(final long... values) {
    return new Builder(values.length, 8) {
      @Override
      void putValues(long address) {
        Platform.putLongs(address, values);
      }
    };
  }

  /**
   * Create a vector from a double array.
   */
  public static Builder vec(final double... values) {
    return new Builder(values.length, 8) {
      @Override
      void putValues(long address) {
        Platform.putDoubles(address, values);
      }
    };
  }

  /**
   * Create a vector from a struct array. Note that this will copy the contents of the structure,
   * but not the vectors it is pointing to.
   */
  public static Builder vec(final WeldStruct... values) {
    // Validate the length of the passed
    long size = 0;
    for (WeldStruct value : values) {
      if (size == 0 && value.size() % 8 != 0) {
        throw new IllegalArgumentException("Struct size (" + value.size() + ") is not 8 byte aligned.");
      } else if (size == 0) {
        size = value.size();
      } else if (size != value.size()) {
        throw new IllegalArgumentException("Struct size (" + value.size() + ") does not match expected size.");
      }
    }
    // Create the vector.
    return new Builder(values.length, (int) size) {
      @Override
      void putValues(long address) {
        for (int i = 0; i < values.length; i++) {
          final WeldStruct value = values[i];
          Platform.copyMemory(value.address(), address + i * elementSize, elementSize);
        }
      }
    };
  }

  /**
   * Create a vector from a vec array.
   */
  public static Builder vec(final WeldVec... values) {
    return new Builder(values.length, 16) {
      @Override
      void putValues(long address) {
        for (int i = 0; i < values.length; i++) {
          final WeldVec value = values[i];
          final long offset = address + i * 16;
          Platform.putLong(offset, value.address());
          Platform.putLong(offset+ 8, value.numElements());
        }
      }
    };
  }

  /**
   * Helper class for building complex serialized vectors.
   */
  abstract static class Builder {
    final int numElements;

    final int elementSize;

    int size() {
      return numElements * elementSize;
    }

    Builder(int numElements, int elementSize) {
      super();
      this.numElements = numElements;
      this.elementSize = elementSize;
    }

    abstract void putValues(long address);
  }
}
