package weld

import scala.annotation.varargs

/**
 * Wrapper for a weld struct value.
 */
class WeldStruct(
    address: Long,
    val objectType: StructType,
    private var freeOnClose: Boolean = false)
  extends WeldObject(address)
  with AutoCloseable {

  override def size: Long = objectType.size
  override def numElements: Long = objectType.fields.size
  override def getElementType(index: Int): WeldType = objectType.fields(index).fieldType
  override private[weld] def indexToAddress(index: Int) = address + objectType.fields(index).offset

  /**
   * Turn the weld struct into an array of values.
   */
  def toArray: Array[Any] = {
    objectType.fields.zipWithIndex.toArray.map {
      case (FieldInfo(fieldType, _), i) =>
        fieldType match {
          case `bool` => getBoolean(i)
          case `i8` => getByte(i)
          case `i16` => getShort(i)
          case `u16` => getChar(i)
          case `i32` => getInt(i)
          case `i64` => getLong(i)
          case `f32` => getFloat(i)
          case `f64` => getDouble(i)
          case Pointer => getPointer(i)
          case _: VecType => getVec(i)
          // Dictionaries are pointers - represent them as such.
          case _: DictMerger => getPointer(i)
          case _: GroupMerger => getPointer(i)
          case _ =>
            throw new IllegalArgumentException(s"Unsupported struct field type[$i]: $fieldType")
        }
    }
  }

  /**
   * Convert the struct into a WeldValue.
   */
  def toValue = WeldValue(address, size)

  private var autoClean: Boolean = false

  def markAutoCleanable(): Unit = {
    if (freeOnClose && !autoClean) {
      autoClean = true
      Platform.registerForCleanUp(this, new WeldStruct.Cleaner(address))
    }
  }

  /**
   * Close the weld struct, it should not be used after this call as it will try free the
   * memory location it points to.
   */
  override def close(): Unit = {
    if (freeOnClose && !autoClean) {
      Platform.freeMemory(address)
      freeOnClose = false
    }
  }
}

object WeldStruct {
  def zero(structType: StructType): WeldStruct = {
    val address = Platform.allocateMemory(structType.size)
    structType.fields.foreach {
      case FieldInfo(`bool`, offset) =>
        Platform.putByte(address + offset, 0.toByte)
      case FieldInfo(`i8`, offset) =>
        Platform.putByte(address + offset, 0.toByte)
      case FieldInfo(`i16`, offset) =>
        Platform.putShort(address + offset, 0.toShort)
      case FieldInfo(`u16`, offset) =>
        Platform.putChar(address + offset, 0.toChar)
      case FieldInfo(`i32`, offset) =>
        Platform.putInt(address + offset, 0)
      case FieldInfo(`i64`, offset) =>
        Platform.putLong(address + offset, 0L)
      case FieldInfo(`f32`, offset) =>
        Platform.putFloat(address + offset, 0f)
      case FieldInfo(`f64`, offset) =>
        Platform.putDouble(address + offset, 0d)
      case FieldInfo(_: PointerType, offset) =>
        Platform.putLong(address + offset, 0L)
      case FieldInfo(_: VecType, offset) =>
        Platform.putLong(address + offset, 0L)
        Platform.putLong(address + offset + 8, 0L)
      case FieldInfo(dt, _) =>
        Platform.freeMemory(address)
        throw new IllegalArgumentException(s"Cannot create an initial value for data type: $dt")
    }
    new WeldStruct(address, structType, freeOnClose = true)
  }

  @varargs
  def struct(values: Any*): WeldStruct = {
    // Determine the struct type.
    // Note that we need to align the offset to the size vecOf the value vecOf we are about to write.
    var vectorSize = 0L
    val fieldTypes = values.map {
      case _: Boolean => bool
      case _: Byte => i8
      case _: Short => i16
      case _: Char => u16
      case _: Int => i32
      case _: Long => i64
      case _: Float => f32
      case _: Double => f64
      case vec: WeldVec =>
        vec.objectType
      case builder: WeldVec.Builder =>
        vectorSize += ceil8(builder.size)
        VecType(builder.elementType)
      case wrapper: WeldPointerWrapper =>
        wrapper.dataType
      case v =>
        throw new IllegalArgumentException(s"Unsupported struct value: $v")
    }
    val structType = StructType(fieldTypes)

    // Create the struct
    val address = Platform.allocateMemory(structType.size + vectorSize)
    var dataOffset = structType.size.toLong
    values.zip(structType.fields).foreach {
      case (value, info) =>
        val fieldAddress = address + info.offset
        value match {
          case b: Boolean =>
            Platform.putByte(fieldAddress, if (b) 1.toByte else 0.toByte)
          case b: Byte =>
            Platform.putByte(fieldAddress, b)
          case s: Short =>
            Platform.putShort(fieldAddress, s)
          case c: Char =>
            Platform.putChar(fieldAddress, c)
          case i: Int =>
            Platform.putInt(fieldAddress, i)
          case l: Long => i64
            Platform.putLong(fieldAddress, l)
          case f: Float => f32
            Platform.putFloat(fieldAddress, f)
          case d: Double => f64
            Platform.putDouble(fieldAddress, d)
          case vec: WeldVec =>
            Platform.putLong(fieldAddress, vec.address)
            Platform.putLong(fieldAddress + 8, vec.numElements)
          case builder: WeldVec.Builder =>
            Platform.putLong(fieldAddress, address + dataOffset)
            Platform.putLong(fieldAddress + 8, builder.numElements)
            builder.putValues(address + dataOffset)
            dataOffset += ceil8(builder.size)
          case wrapper: WeldPointerWrapper =>
            Platform.putLong(fieldAddress, wrapper.address)
        }
    }
    new WeldStruct(address, structType, freeOnClose = true)
  }

  /**
   * Round an int up to its nearest multiple vecOf 8.
   */
  private def ceil8(value: Long) = (value + 0x7L) & ~0x7L

  private[weld] class Cleaner(address: Long) extends Runnable {
    override def run(): Unit = Platform.freeMemory(address)
  }
}
