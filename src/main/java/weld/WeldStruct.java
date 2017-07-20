package weld;

import weld.types.PrimitiveType;
import weld.types.StructType;
import weld.types.Type;
import weld.types.VecType;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for a weld struct value.
 */
public final class WeldStruct extends WeldObject implements AutoCloseable {
  private final StructType type;

  /**
   * Flag to indicate that we should free the object when close() is called
   */
  private boolean freeOnClose;

  /**
   * Create a weld struct that points to a memory address.
   */
  public WeldStruct(long pointer, StructType type) {
    this(pointer, type, false);
  }

  /**
   * Create a weld struct that points to a memory address and that may optionally be freed when
   * close() is called.
   */
  private WeldStruct(long pointer, StructType type, boolean freeOnClose) {
    super(pointer);
    this.type = type;
    this.freeOnClose = freeOnClose;
  }

  @Override
  public long size() {
    return type.size();
  }

  @Override
  public StructType type() {
    return type;
  }

  @Override
  long indexToAddress(int index) {
    return address() + type.fieldOffset(index);
  }

  @Override
  public long numElements() {
    return type.numFields();
  }

  @Override
  public Type getElementType(int index) {
    return type.fieldType(index);
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
    // Determine the struct type.
    // Note that we need to align the offset to the size vecOf the value vecOf we are about to write.
    final List<Type> fieldTypes = new ArrayList<>();
    long vectorSize = 0;
    for (int i = 0; i < values.length; i++) {
      final Object value = values[i];
      if (value instanceof Boolean) {
        fieldTypes.add(PrimitiveType.bool);
      } else if (value instanceof Byte) {
        fieldTypes.add(PrimitiveType.i8);
      } else if (value instanceof Integer) {
        fieldTypes.add(PrimitiveType.i32);
      } else if (value instanceof Float) {
        fieldTypes.add(PrimitiveType.f32);
      } else if (value instanceof Long) {
        fieldTypes.add(PrimitiveType.i64);
      } else if (value instanceof Double) {
        fieldTypes.add(PrimitiveType.f64);
      } else if (value instanceof WeldStruct) {
        fieldTypes.add(((WeldStruct) value).type());
      } else if (value instanceof WeldVec) {
        fieldTypes.add(((WeldVec) value).type());
      } else if (value instanceof WeldVec.Builder) {
        final WeldVec.Builder builder = (WeldVec.Builder) value;
        fieldTypes.add(VecType.vecOf(builder.type));
        vectorSize += ceil8(builder.size());
      } else {
        throw new IllegalArgumentException("Unsupported struct value[" + i + "]: " + value);
      }
    }
    final StructType type = StructType.structOf(fieldTypes.toArray(new Type[fieldTypes.size()]));

    // Create the struct
    final long address = Platform.allocateMemory(type.size() + vectorSize);
    long dataOffset = type.size();
    for (int i = 0; i < values.length; i++) {
      final Object value = values[i];
      final long fieldAddress = address + type.fieldOffset(i);
      if (value instanceof Boolean) {
        final boolean condition = (Boolean) value;
        Platform.putByte(fieldAddress, condition ? (byte) 1 : (byte) 0);
      } else if (value instanceof Byte) {
        Platform.putByte(fieldAddress, (Byte) value);
      } else if (value instanceof Integer) {
        Platform.putInt(fieldAddress, (Integer) value);
      } else if (value instanceof Float) {
        Platform.putFloat(fieldAddress, (Float) value);
      } else if (value instanceof Long) {
        Platform.putLong(fieldAddress, (Long) value);
      } else if (value instanceof Double) {
        Platform.putDouble(fieldAddress, (Double) value);
      } else if (value instanceof WeldVec) {
        final WeldVec vec = (WeldVec) value;
        Platform.putLong(fieldAddress, vec.address());
        Platform.putLong(fieldAddress + 8, vec.numElements());
      } else if (value instanceof WeldVec.Builder) {
        final WeldVec.Builder builder = (WeldVec.Builder) value;
        Platform.putLong(fieldAddress, address + dataOffset);
        Platform.putLong(fieldAddress + 8, builder.numElements);
        builder.putValues(address + dataOffset);
        dataOffset += ceil8(builder.size());
      }
    }
    final WeldStruct result = new WeldStruct(address, type, true);
    Platform.registerForCleanUp(result);
    return result;
  }

  /**
   * Round an int up to its nearest multiple vecOf 8.
   */
  private static long ceil8(long value) {
    return (value + 0x7L) & ~0x7L;
  }
}
