package weld;

import weld.types.PrimitiveType;
import weld.types.Type;
import weld.types.VecType;

/**
 * A typed base class for complex weld values.
 */
public abstract class WeldObject {
  /**
   * Address vecOf the underlying data.
   */
  private final long address;

  /**
   * Create a weld object that points to a memory block allocated using
   * sun.misc.Unsafe or native code.
   */
  WeldObject(long address) {
    super();
    this.address = address;
  }

  /**
   * Translate an offset index to its address.
   */
  long indexToAddress(int index, Type type) {
    assert index >= 0 && index < numElements() :  "Index(" + index + ") must be >= 0 and < " + numElements();
    final Type actualField = getElementType(index);
    assert type.equals(actualField) :"Expected a " + type + " at index(" + index + "), but found a '" + actualField + "' instead.";
    return indexToAddress(index);
  }

  /**
   * Translate an offset index to its address.
   */
  abstract long indexToAddress(int index);

  /**
   * Get the address to the backing data.
   */
  public long address() {
    return address;
  }

  /**
   * Get the size vecOf object in bytes.
   */
  public abstract long size();

  /**
   * Get the number vecOf elements in the object.
   */
  public abstract long numElements();

  /**
   * Get the element type at the given index.
   */
  public abstract Type getElementType(int index);

  /**
   * Get the type vecOf the weld object.
   */
  public abstract Type type();

  /**
   * Get the boolean value at the given index.
   */
  public boolean getBoolean(int index) {
    return Platform.getByte(indexToAddress(index, PrimitiveType.bool)) == (byte) 1;
  }

  /**
   * Get the byte value at the given offset.
   */
  public byte getByte(int index) {
    return Platform.getByte(indexToAddress(index, PrimitiveType.i8));
  }

  /**
   * Get the int value at the given offset.
   */
  public int getInt(int index) {
    return Platform.getInt(indexToAddress(index, PrimitiveType.i32));
  }

  /**
   * Get the long value at the given offset.
   */
  public long getLong(int index) {
    return Platform.getLong(indexToAddress(index, PrimitiveType.i64));
  }

  /**
   * Get the float value at the given offset.
   */
  public float getFloat(int index) {
    return Platform.getFloat(indexToAddress(index, PrimitiveType.f32));
  }

  /**
   * Get the double value at the given offset.
   */
  public double getDouble(int index) {
    return Platform.getDouble(indexToAddress(index, PrimitiveType.f64));
  }

  /**
   * Get the pointer at the given offset.
   */
  public long getPointer(int index) {
    return Platform.getLong(indexToAddress(index, PrimitiveType.Pointer));
  }

  /**
   * Get the vector at the given index.
   */
  public WeldVec getVec(int index) {
    final Type elementType = getElementType(index);
    assert elementType instanceof VecType : "Expected a vector at index(" + index + "), but found a '" + elementType + "' instead.";
    final long address = indexToAddress(index);
    final long pointer = Platform.getLong(address);
    final long numElements = Platform.getLong(address + 8);
    return new WeldVec(pointer, numElements, (VecType) elementType);
  }
}
