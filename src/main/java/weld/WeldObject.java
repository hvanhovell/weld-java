package weld;

/**
 * Base class for complex weld values.
 *
 * Note that
 */
public abstract class WeldObject {
  /**
   * Address of the underlying data.
   */
  private final long address;

  /**
   * Size of object in bytes.
   */
  private final long size;

  /**
   * Create a weld object that points to a memory block allocated using
   * sun.misc.Unsafe or native code.
   */
  WeldObject(long address, long size) {
    super();
    if (size < 0) {
      throw new IllegalArgumentException("Size(" + size + ") must be >= 0.");
    }
    this.address = address;
    this.size = size;
  }

  /**
   * Translate an offset index to its address.
   */
  long offsetToAddress(int offset, int bytesToRead) {
    assert offset >= 0 && offset + bytesToRead <= size : "offset(" + offset + ") should be >= 0 and < " + size;
    return address + offset;
  }

  /**
   * Get the address to the backing data.
   */
  public long address() {
    return address;
  }

  /**
   * Get the size of object in bytes.
   */
  public long size() {
    return size;
  }

  /**
   * Get the boolean value at the given offset.
   */
  public boolean getBoolean(int offset) {
    return Platform.getByte(offsetToAddress(offset, 1)) == (byte) 1;
  }

  /**
   * Get the byte value at the given offset.
   */
  public byte getByte(int offset) {
    return Platform.getByte(offsetToAddress(offset,1 ));
  }

  /**
   * Get the int value at the given offset.
   */
  public int getInt(int offset) {
    return Platform.getInt(offsetToAddress(offset, 4));
  }

  /**
   * Get the long value at the given offset.
   */
  public long getLong(int offset) {
    return Platform.getLong(offsetToAddress(offset, 8));
  }

  /**
   * Get the float value at the given offset.
   */
  public float getFloat(int offset) {
    return Platform.getFloat(offsetToAddress(offset, 4));
  }

  /**
   * Get the double value at the given offset.
   */
  public double getDouble(int offset) {
    return Platform.getDouble(offsetToAddress(offset, 8));
  }

  /**
   * Get the vector at the given index. Note that in case of a struct this will consume two
   * indexes, because a struct is 8 byte aligned.
   */
  public WeldVec getVec(int offset, int elementSize) {
    long ptrAddress = offsetToAddress(offset, 16);
    long vecAddress = Platform.getLong(ptrAddress);
    long vecElements = Platform.getLong(ptrAddress + 8);
    return new WeldVec(vecAddress, vecElements * elementSize, elementSize);
  }
}
