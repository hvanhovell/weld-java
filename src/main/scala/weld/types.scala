package weld

import scala.annotation.varargs

/**
 * A weld data type.
 */
trait WeldType extends Serializable {
  /**
   * Get the name of data type.
   */
  def name: String

  /**
   * Get the alignment of the data type.
   */
  def alignment: Int

  /**
   * Size vecOf the element as used in a struct.
   */
  def size: Int

  override def toString: String = name
}

object UnknownType extends WeldType {
  override def name: String = "UNKNOWN"
  override def alignment: Int = 0
  override def size: Int = 0
}

/**
 * A weld primitive type.
 */
sealed abstract class PrimitiveType(val name: String, val size: Int, val suffix: String = "") extends WeldType {
  override def alignment: Int = size
}

/**
 * Boolean.
 */
object bool extends PrimitiveType("bool", 1)

/**
 * 8-bit signed integer.
 */
object i8 extends PrimitiveType("i8", 1, "C")

/**
 * 32-bit signed integer.
 */
object i32 extends PrimitiveType("i32", 4)

/**
 * 64-bit signed integer.
 */
object i64 extends PrimitiveType("i64", 8, "L")

/**
 * 32-bit floating point.
 */
object f32 extends PrimitiveType("f32", 4, "F")

/**
 * 64-bit floating point.
 */
object f64 extends PrimitiveType("f64", 8)

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

trait PointerType extends WeldType {
  override def alignment: Int = 8
  override def size: Int = 8
}

/**
 * Opaque pointer. This is used to pass weld structures between plans.
 */
object Pointer extends PointerType {
  override def name: String = "ptr"
}

case class DictType(key: WeldType, value: WeldType) extends PointerType {
  override def name: String = s"dict[$key, $value]"
  override def alignment: Int = 8
  override def size: Int = 8
}

trait BuilderType extends PointerType {
  def inputType: WeldType
  def outputType: WeldType
  override def size: Int = 8
  override def alignment: Int = 8
}

sealed trait Aggregator {
  def name: String
}

object PlusAggregator extends Aggregator {
  override def name: String = "+"
}

object TimesAggregator extends Aggregator {
  override def name: String = "*"
}

trait AggregatingBuilderType extends BuilderType {
  def aggregator: Aggregator
}

case class Appender(inputType: WeldType) extends BuilderType {
  override def outputType: WeldType = VecType(inputType)
  override def name: String = s"appender[${inputType.name}]"
}

case class GroupMerger(keyType: WeldType, valueType: WeldType) extends BuilderType {
  override def inputType: WeldType = StructType.structOf(keyType, valueType)
  override def outputType: WeldType = DictType(keyType, VecType(valueType))
  override def name: String = s"groupmerger[${keyType.name}, ${valueType.name}]"
}

case class Merger(inputType: WeldType, aggregator: Aggregator) extends AggregatingBuilderType {
  override def outputType: WeldType = inputType
  override def name: String = s"merger[${inputType.name}, ${aggregator.name}]"
}

case class DictMerger(keyType: WeldType, valueType: WeldType, aggregator: Aggregator) extends AggregatingBuilderType {
  override def inputType: WeldType = StructType.structOf(keyType, valueType)
  override def outputType: WeldType = DictType(keyType, valueType)
  override def name: String = s"dictmerger[${keyType.name}, ${valueType.name}, ${aggregator.name}]"
}

case class VecMerger(inputType: WeldType, aggregator: Aggregator) extends AggregatingBuilderType {
  override def outputType: WeldType = VecType(inputType)
  override def name: String = s"vecmerger[${inputType.name}, ${aggregator.name}]"
}
