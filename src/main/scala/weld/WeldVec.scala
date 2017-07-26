package weld

/**
 * Wrapper for a weld vec value.
 */
class WeldVec(
    address: Long,
    val objectType: VecType,
    val numElements: Long)
  extends WeldObject(address) {
  override def size: Long = numElements * elementSize
  override def getElementType(index: Int): WeldType = objectType.elementType
  override private[weld] def indexToAddress(index: Int) = address + index * elementSize

  /**
   * Get the size of a single element in the vector.
   */
  def elementSize: Long = objectType.elementType.size

  /**
   * Get a struct from the vector at the given offset.
   */
  def getStruct(index: Int): WeldStruct = {
    val elementType = objectType.elementType
    assert(elementType.isInstanceOf[StructType], s"Expected a struct but found a '$elementType' instead.")
    new WeldStruct(indexToAddress(index), elementType.asInstanceOf[StructType])
  }
}

object WeldVec {
  /**
   * Helper class for building vectors.
   */
  abstract class Builder(val numElements: Int, val elementType: WeldType) {
    def size: Int = numElements * elementType.size
    def putValues(address: Long): Unit
  }

  /**
   * Create a vector from a boolean array.
   */
  def vec(values: Array[Boolean]): Builder = new Builder(values.length, bool) {
    override def putValues(address: Long): Unit = {
      var i = 0
      while (i < numElements) {
        Platform.putByte(address + i, if (values(i)) 1.toByte else 0.toByte)
        i += 1
      }
    }
  }

  /**
   * Create a vector from a byte array.
   */
  def vec(values: Array[Byte]): Builder = new Builder(values.length, i8) {
    override def putValues(address: Long): Unit = {
      Platform.putBytes(address, values)
    }
  }

  /**
   * Create a vector from a string.
   */
  def vec(text: String): Builder = vec(text.getBytes)

  /**
   * Create a vector from a int array.
   */
  def vec(values: Array[Int]): Builder = new Builder(values.length, i32) {
    override def putValues(address: Long): Unit = {
      Platform.putInts(address, values)
    }
  }

  /**
   * Create a vector from a long array.
   */
  def vec(values: Array[Long]): Builder = new Builder(values.length, i64) {
    override def putValues(address: Long): Unit = {
      Platform.putLongs(address, values)
    }
  }

  /**
   * Create a vector from a float array.
   */
  def vec(values: Array[Float]): Builder = new Builder(values.length, f32) {
    override def putValues(address: Long): Unit = {
      Platform.putFloats(address, values)
    }
  }

  /**
   * Create a vector from a double array.
   */
  def vec(values: Array[Double]): Builder = new Builder(values.length, f64) {
    override def putValues(address: Long): Unit = {
      Platform.putDoubles(address, values)
    }
  }

  /**
   * Infer the object type for a seq of [[WeldObject]].
   */
  private def inferAndValidateObjectType(values: Seq[WeldObject]): WeldType = {
    require(values.nonEmpty, "Cannot infer a type on an empty seq.")
    val objectType = values.head.objectType
    values.tail.foreach { value =>
      if (objectType != value.objectType) {
        throw new IllegalArgumentException(
          s"Values should not contain multiple types: $objectType != ${value.objectType}")
      }
    }
    objectType
  }

  /**
   * Create a vector from a struct array. Note that this will copy the contents vecOf the structure,
   * but not the vectors it is pointing to.
   */
  def vec(values: Array[WeldStruct]): Builder = {
    new Builder(values.length, inferAndValidateObjectType(values)) {
      override def putValues(address: Long): Unit = {
        var i = 0
        while (i < numElements) {
          val value = values(i)
          Platform.copyMemory(value.address, address + i * elementType.size, elementType.size)
          i += 1
        }
      }
    }
  }

  /**
   * Create a vector from a vec array.
   */
  def vec(values: Array[WeldVec]) = {
    new Builder(values.length, inferAndValidateObjectType(values)) {
      override def putValues(address: Long): Unit = {
        var i = 0
        while (i < numElements) {
          val value = values(i)
          val offset = address + i * 16
          Platform.putLong(offset, value.address)
          Platform.putLong(offset + 8, value.numElements)
          i += 1
        }
      }
    }
  }
}
