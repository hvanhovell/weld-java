package weld;

import weld.types.PrimitiveType;
import weld.types.StructType;
import weld.types.Type;
import weld.types.VecType;

/**
 * Wrapper for a weld vec value.
 */
public final class WeldVec extends WeldObject {
  private final VecType type;

  private final long numElements;

  /**
   * Create a weld vec that points to a memory block allocated using
   * sun.misc.Unsafe or native code.
   */
  public WeldVec(long pointer, long numElements, VecType type) {
    super(pointer);
    if (numElements < 0) {
      throw new IllegalArgumentException("NumElements(" + numElements + ") must be >= 0.");
    }
    this.numElements = numElements;
    this.type = type;
  }

  @Override
  public long size() {
    return numElements * elementSize();
  }

  @Override
  public VecType type() {
    return type;
  }

  @Override
  long indexToAddress(int index) {
    return address() + index * elementSize();
  }

  @Override
  public Type getElementType(int index) {
    return type.elementType();
  }

  /**
   * Get the number vecOf elements in the vector.
   */
  @Override
  public long numElements() {
    return numElements;
  }

  /**
   * Get the size vecOf single element in the vector.
   */
  public long elementSize() {
    return type.elementType().size();
  }


  /**
   * Get a struct from the vector at the given offset.
   */
  public WeldStruct getStruct(int index) {
    final Type elementType = type.elementType();
    assert elementType instanceof StructType : "Expected a struct at index(" + index + "), but found a '" + elementType + "' instead.";
    return new WeldStruct(indexToAddress(index), (StructType) elementType);
  }

  /**
   * Create a vector from a memory location.
   */
  public static WeldVec fromAddress(long pointer, long numElements, VecType type) {
    return new WeldVec(pointer, numElements, type);
  }

  /**
   * Create a vector from a boolean array.
   */
  public static Builder vec(final boolean... values) {
    return new Builder(values.length, PrimitiveType.bool) {
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
    return new Builder(values.length, PrimitiveType.i8) {
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
    return new Builder(values.length, PrimitiveType.i32) {
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
    return new Builder(values.length, PrimitiveType.f32) {
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
    return new Builder(values.length, PrimitiveType.i64) {
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
    return new Builder(values.length, PrimitiveType.f64) {
      @Override
      void putValues(long address) {
        Platform.putDoubles(address, values);
      }
    };
  }

  /**
   * Create a vector from a struct array. Note that this will copy the contents vecOf the structure,
   * but not the vectors it is pointing to.
   */
  public static Builder vec(final WeldStruct... values) {
    return new Builder(values.length, inferAndValidateObjectType(values)) {
      @Override
      void putValues(long address) {
        for (int i = 0; i < values.length; i++) {
          final WeldStruct value = values[i];
          Platform.copyMemory(value.address(), address + i * type.size(), type.size());
        }
      }
    };
  }

  /**
   * Create a vector from a vec array.
   */
  public static Builder vec(final WeldVec... values) {
    return new Builder(values.length, inferAndValidateObjectType(values)) {
      @Override
      void putValues(long address) {
        for (int i = 0; i < values.length; i++) {
          final WeldVec value = values[i];
          final long offset = address + i * 16;
          Platform.putLong(offset, value.address());
          Platform.putLong(offset + 8, value.numElements());
        }
      }
    };
  }

  private static Type inferAndValidateObjectType(final WeldObject[] values) {
    Type type = null;
    for (WeldObject value : values) {
      if (type == null) {
        type = value.type();
      } else if (!type.equals(value.type())) {
        throw new IllegalArgumentException("Values cannot different types: " + type + " != " + value.type());
      }
    }
    if (type == null) {
      throw new IllegalArgumentException("Cannot infer a type");
    }
    return type;
  }

  /**
   * Helper class for building complex serialized vectors.
   */
  abstract static class Builder {
    final int numElements;

    final Type type;

    int size() {
      return numElements * type.size();
    }

    Builder(int numElements, Type type) {
      super();
      this.numElements = numElements;
      this.type = type;
    }

    abstract void putValues(long address);
  }
}
