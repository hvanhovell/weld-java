package weld

import scala.annotation.varargs

/**
 * A weld data type.
 */
trait WeldType {
  /**
   * Get the name vecOf data type.
   */
  def name: String

  /**
   * Get the alignment vecOf the data type.
   */
  def alignment: Int

  /**
   * Size vecOf the element as used in a struct.
   */
  def size: Int

  override def toString: String = name
}

/**
 * A weld primitive type.
 */
sealed abstract class Primitive(val name: String, val size: Int) extends WeldType {
  override def alignment: Int = size
}

/**
 * Boolean.
 */
object bool extends Primitive("bool", 1)

/**
 * 8-bit signed integer.
 */
object i8 extends Primitive("i8", 1)

/**
 * 32-bit signed integer.
 */
object i32 extends Primitive("i32", 4)

/**
 * 64-bit signed integer.
 */
object i64 extends Primitive("i64", 8)

/**
 * 32-bit floating point.
 */
object f32 extends Primitive("f32", 4)

/**
 * 64-bit floating point.
 */
object f64 extends Primitive("f64", 8)

/**
 * Opaque pointer. This is used to pass weld structures between plans.
 */
object Pointer extends Primitive("ptr", 8)

/**
 * Vector type.
 */
case class VecType(elementType: WeldType) extends WeldType {
  require(elementType != null, "elementType must not be null")
  override def alignment = 8
  override def size = 16
  override lazy val name = s"vec[$elementType]"
}

object VecType {
  def vecOf(elementType: WeldType): VecType = VecType(elementType)
}

/**
 * Struct type.
 */
case class StructType private(size: Int, fields: IndexedSeq[FieldInfo]) extends WeldType {
  override lazy val name: String = fields.map(_.fieldType).mkString("{", ", ", "}")
  override def alignment: Int = 8
}

object StructType {
  @varargs
  def structOf(fieldTypes: WeldType*): StructType = apply(fieldTypes)

  def apply(fieldTypes: Seq[WeldType]): StructType = {
    var size = 0
    val fields = fieldTypes.toIndexedSeq.map { field =>
      size = ceil(size, field.alignment)
      val info = FieldInfo(field, size)
      size += field.size
      info
    }
    size = ceil(size, 8)
    StructType(size, fields)
  }

  def ceil(offset: Int, alignment: Int): Int = {
    val next = offset + alignment - 1
    next - (next % alignment)
  }
}

case class FieldInfo(fieldType: WeldType, offset: Int)
