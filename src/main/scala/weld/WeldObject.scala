package weld

/**
 * A typed base class for complex weld values.
 */
abstract class WeldObject(val address: Long) {
  /**
   * Get the size vecOf object in bytes.
   */
  def size: Long

  /**
   * Get the number vecOf elements in the object.
   */
  def numElements: Long

  /**
   * Get the element type at the given index.
   */
  def getElementType(index: Int): WeldType

  /**
   * Get the type vecOf the weld object.
   */
  def objectType: WeldType

  /**
   * Translate an offset index to its address.
   */
  private[weld] def indexToAddress(index: Int, elementType: WeldType): Long = {
    assert(index >= 0 && index < numElements, s"Index($index) must be >= 0 and < $numElements")
    val actualElementType = getElementType(index)
    assert(elementType == actualElementType, s"Expected a '$elementType' at index($index), but found a '$actualElementType' instead.")
    indexToAddress(index)
  }

  /**
   * Translate an offset index to its address.
   */
  private[weld] def indexToAddress(index: Int): Long

  /**
   * Get the boolean value at the given index.
   */
  def getBoolean(index: Int): Boolean = Platform.getByte(indexToAddress(index, bool)) == 1.toByte

  /**
   * Get the byte value at the given offset.
   */
  def getByte(index: Int): Byte = Platform.getByte(indexToAddress(index, i8))

  /**
   * Get the int value at the given offset.
   */
  def getInt(index: Int): Int = Platform.getInt(indexToAddress(index, i32))

  /**
   * Get the long value at the given offset.
   */
  def getLong(index: Int): Long = Platform.getLong(indexToAddress(index, i64))

  /**
   * Get the float value at the given offset.
   */
  def getFloat(index: Int): Float = Platform.getFloat(indexToAddress(index, f32))

  /**
   * Get the double value at the given offset.
   */
  def getDouble(index: Int): Double = Platform.getDouble(indexToAddress(index, f64))

  /**
   * Get the pointer at the given offset.
   */
  def getPointer(index: Int): WeldPointerWrapper = {
    val elementType = getElementType(index)
    assert(getElementType(index).isInstanceOf[PointerType], s"Expected a pointer at index($index), but found a '$elementType' instead.")
    WeldPointerWrapper(Platform.getLong(indexToAddress(index)), elementType.asInstanceOf[PointerType])
  }

  /**
   * Get the vector at the given index.
   */
  def getVec(index: Int): WeldVec = {
    val elementType = getElementType(index)
    assert(elementType.isInstanceOf[VecType], s"Expected a vector at index($index), but found a '$elementType' instead.")
    val address = indexToAddress(index)
    val pointer = Platform.getLong(address)
    val numElements = Platform.getLong(address + 8)
    new WeldVec(pointer, elementType.asInstanceOf[VecType], numElements)
  }
}
