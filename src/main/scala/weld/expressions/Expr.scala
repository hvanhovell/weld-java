package weld.expressions

import weld._

trait ExprLike extends Serializable {
  def resolved: Boolean = children.forall(_.resolved)
  def dataType: WeldType
  def children: Seq[Expr]
  def references: Seq[Expr] = children.flatMap(_.references)
  def foldable: Boolean = children.forall(_.foldable)
  lazy val desc: String = IndentedDescBuilder().append(this).desc
  lazy val flatDesc: String = SimpleDescBuilder().append(this).desc
  def buildDesc(builder: DescBuilder): Unit
  override def toString: String = desc
}

// TODO add annotations.
trait Expr extends ExprLike {
  protected def withNewChildren(children: Seq[Expr]): Expr
  final def mapChildren(f: Expr => Expr): Expr = withNewChildren(children.map(f))

  final def transformDown(f: PartialFunction[Expr, Expr]): Expr = {
    f.applyOrElse(this, identity[Expr]).mapChildren(_.transformDown(f))
  }

  final def transformUp(f: PartialFunction[Expr, Expr]): Expr = {
    val transformed = mapChildren(_.transformUp(f))
    val target = if ((transformed eq this) || (transformed == this)) this else transformed
    f.applyOrElse(target, identity[Expr])
  }
}

trait FunctionDesc extends Expr {
  def fn: String
  override def buildDesc(builder: DescBuilder): Unit = {
    builder.appendPrefix(fn).append("(", ", ", ")", children)
  }
}

trait LeafExpr extends Expr {
  override def resolved: Boolean = dataType != UnknownType
  override def children: Seq[Expr] = Seq.empty
  override protected def withNewChildren(children: Seq[Expr]): Expr = this
}

trait UnaryExpr extends Expr {
  def child: Expr
  override def children: Seq[Expr] = Seq(child)
  override def resolved: Boolean = child.resolved
  override def dataType: WeldType = child.dataType
  override protected final def withNewChildren(children: Seq[Expr]): Expr = withNewChild(children.head)
  protected def withNewChild(child: Expr): Expr
}

trait BinaryExpr extends Expr {
  def left: Expr
  def right: Expr
  override def children: Seq[Expr] = Seq(left, right)
  override def resolved: Boolean = left.resolved && right.resolved
  override protected final def withNewChildren(children: Seq[Expr]): Expr = {
    val Seq(newLeft, newRight) = children
    withNewChildren(newLeft, newRight)
  }
  protected def withNewChildren(left: Expr, right: Expr): Expr
}

case class Identifier(name: String, dataType: WeldType = UnknownType) extends LeafExpr {
  require(name != null && name != "")
  override def references: Seq[Expr] = Seq(this)
  override def buildDesc(builder: DescBuilder): Unit = builder.append(name)
}

case class Parameter(id: Identifier) extends ExprLike {
  override def dataType: WeldType = id.dataType
  override def children: Seq[Expr] = Seq.empty
  override def buildDesc(builder: DescBuilder): Unit = {
    builder.append(id.name).append(": ").append(dataType.name)
  }
}

case class Literal private(value: Any, dataType: PrimitiveType) extends LeafExpr {
  require(value != null)
  override def references: Seq[Expr] = Seq()
  override def buildDesc(builder: DescBuilder): Unit = {
    builder.append(value.toString).append(dataType.suffix)
  }
}

object Literal {
  def apply(value: Any): Literal = {
    val dataType = value match {
      case _: Boolean => bool
      case _: Byte => i8
      case _: Int => i32
      case _: Long => i64
      case _: Float => f32
      case _: Double => f64
      case _ => throw new IllegalArgumentException(s"Cannot create a literal for: $value")
    }
    Literal(value, dataType)
  }
}

case class Cast(child: Expr, override val dataType: PrimitiveType) extends UnaryExpr with FunctionDesc {
  override def fn: String = dataType.name
  override protected def withNewChild(newChild: Expr): Cast = copy(newChild)
}

case class Broadcast(child: Expr) extends UnaryExpr with FunctionDesc {
  override def dataType: WeldType = VecType(child.dataType)
  override def fn: String = "broadcast"
  override protected def withNewChild(newChild: Expr): Broadcast = copy(newChild)
}

case class ToVec(child: Expr) extends UnaryExpr with FunctionDesc {
  override def dataType: WeldType = {
    val DictType(keyType, valueType) = child.dataType
    VecType(StructType.structOf(keyType, valueType))
  }
  override def resolved: Boolean = child.resolved && child.dataType.isInstanceOf[DictType]
  override def fn: String = "tovec"
  override protected def withNewChild(newChild: Expr): ToVec = copy(newChild)
}

case class MakeStruct(children: Seq[Expr]) extends Expr {
  override def dataType: WeldType = StructType.structOf(children.map(_.dataType): _*)
  override def buildDesc(builder: DescBuilder): Unit = {
    builder.append("{", ", ", "}", children)
  }
  override protected def withNewChildren(children: Seq[Expr]): MakeStruct = copy(children)
}

case class MakeVector private(children: Seq[Expr], elementType: WeldType) extends Expr {
  override def dataType: WeldType = VecType(elementType)
  override def resolved: Boolean = {
    elementType != UnknownType && super.resolved && children.forall(_.dataType == elementType)
  }
  override def buildDesc(builder: DescBuilder): Unit = {
    builder.append("[", ", ", "]", children)
  }
  override protected def withNewChildren(children: Seq[Expr]): MakeVector = copy(children)
}

object MakeVector {
  def apply(children: Seq[Expr]): MakeVector = {
    require(children.nonEmpty)
    val elementType = children.map(_.dataType)
      .filterNot(_ == UnknownType)
      .headOption
      .getOrElse(UnknownType)
    MakeVector(children, elementType)
  }

  /**
   * Create a [[MakeVector]] statement that produces an empty vector.
   */
  def empty(elementType: WeldType): MakeVector = MakeVector(Seq.empty, elementType)
}

case class GetField(child: Expr, index: Int) extends UnaryExpr {
  override def resolved: Boolean = child.resolved && child.dataType.isInstanceOf[StructType]
  override def dataType: WeldType = {
    val structType = child.dataType.asInstanceOf[StructType]
    structType.fields(index).fieldType
  }
  override def buildDesc(builder: DescBuilder): Unit = builder.append(s"${child.desc}.$$$index")
  override protected def withNewChild(newChild: Expr): GetField = copy(newChild)
}

case class Length(child: Expr) extends UnaryExpr with FunctionDesc {
  override def resolved: Boolean = child.resolved && child.dataType.isInstanceOf[VecType]
  override def dataType: WeldType = i64
  override def fn: String = "len"
  override protected def withNewChild(newChild: Expr): Length = copy(newChild)
}

// TODO The weld expression language does not support the dict version of lookup.
case class Lookup(left: Expr, right: Expr) extends BinaryExpr with FunctionDesc {
  override def fn: String = "lookup"
  override def resolved: Boolean = (left.dataType, right.dataType) match {
    case _ if !super.resolved => false
    case (_: VecType, `i64`) => true
    case (DictType(keyType, _), lookupType) => keyType == lookupType
    case _ => false
  }
  override def dataType: WeldType = left.dataType match {
    case VecType(elementType) => elementType
    case DictType(_, valueType) => valueType
    case _ => UnknownType
  }
  override protected def withNewChildren(left: Expr, right: Expr): Lookup = copy(left, right)
}

case class KeyExists(left: Expr, right: Expr) extends BinaryExpr with FunctionDesc {
  override def fn: String = "keyexists"
  override def resolved: Boolean = (left.dataType, right.dataType) match {
    case _ if !super.resolved => false
    case (DictType(keyType, _), lookupType) => keyType == lookupType
    case _ => false
  }
  override def dataType: WeldType = bool
  override protected def withNewChildren(left: Expr, right: Expr): KeyExists = copy(left, right)
}

case class Slice(vector: Expr, offset: Expr, length: Expr) extends Expr with FunctionDesc {
  override def fn: String = "slice"
  override def children: Seq[Expr] = Seq(vector, offset, length)
  override def dataType: WeldType = vector.dataType
  override def resolved: Boolean = {
    super.resolved &&
      vector.dataType.isInstanceOf[VecType] &&
      offset.dataType == i64 &&
      length.dataType == i64
  }

  override protected def withNewChildren(children: Seq[Expr]): Slice = {
    val Seq(newVector, newOffset, newLength) = children
    copy(newVector, newOffset, newLength)
  }
}

case class Let(name: String, value: Expr, body: Expr) extends Expr {
  override def dataType: WeldType = body.dataType
  override def children: Seq[Expr] = Seq(value, body)
  override def buildDesc(builder: DescBuilder): Unit = {
    builder.appendPrefix(s"let $name = ").append(value).append(";")
    builder.newLine().append(body)
  }
  override protected def withNewChildren(children: Seq[Expr]): Let = {
    val Seq(newValue, newBody) = children
    copy(name, newValue, newBody)
  }
}

abstract class ConditionalExpr(condition: Expr, onTrue: Expr, onFalse: Expr) extends Expr with FunctionDesc {
  override def dataType: WeldType = onTrue.dataType
  override def children: Seq[Expr] = Seq(condition, onTrue, onFalse)
  override def resolved: Boolean = {
    super.resolved && condition.dataType == bool && onTrue.dataType == onFalse.dataType
  }
}

case class Select(condition: Expr, onTrue: Expr, onFalse: Expr) extends ConditionalExpr(condition, onTrue, onFalse) {
  override def fn: String = "select"
  override protected def withNewChildren(children: Seq[Expr]): Select = {
    val Seq(newCondition, newOnTrue, newOnFalse) = children
    copy(newCondition, newOnTrue, newOnFalse)
  }
}

case class If(condition: Expr, onTrue: Expr, onFalse: Expr) extends ConditionalExpr(condition, onTrue, onFalse) {
  override def fn: String = "if"
  override protected def withNewChildren(children: Seq[Expr]): If = {
    val Seq(newCondition, newOnTrue, newOnFalse) = children
    copy(newCondition, newOnTrue, newOnFalse)
  }
}

case class CUDF(name: String, children: Seq[Expr], dataType: WeldType) extends Expr with FunctionDesc {
  override def resolved: Boolean = super.resolved && dataType != UnknownType
  override def fn: String = s"cudf[$name, $dataType]"
  override protected def withNewChildren(children: Seq[Expr]): CUDF = copy(children = children)
}

case class Iterate(left: Expr, right: Expr) extends BinaryExpr with FunctionDesc {
  override def fn: String = "iterate"
  override def dataType: WeldType = left.dataType
  override def resolved: Boolean = (left.dataType, right.dataType) match {
    case _ if !super.resolved => false
    case (inputType, StructType(_, IndexedSeq(FieldInfo(outputType, _), FieldInfo(`bool`, _)))) =>
      inputType == outputType
    case _ => false
  }
  override protected def withNewChildren(left: Expr, right: Expr): Iterate = copy(left, right)
}

case class Lambda(parameters: Seq[Identifier], body: Expr) extends Expr {
  override def dataType: WeldType = body.dataType
  override def children: Seq[Expr] = parameters :+ body
  override def buildDesc(builder: DescBuilder): Unit = {
    builder.append("|", ", ", "|", parameters.map(Parameter)).newLine().append(body)
  }
  override protected def withNewChildren(children: Seq[Expr]): Lambda = {
    val newParameters :+ newBody = children
    copy(newParameters.map(_.asInstanceOf[Identifier]), newBody)
  }
}

// Note that this is NOT an expression because you can only use it as the input to a For expression.
// TODO add fringe & simd versions (this requires some work on the type system).
case class Iter(expr: Expr, startEndStride: Option[(Expr, Expr, Expr)] = None) extends ExprLike {
  private lazy val params: Seq[Expr] = startEndStride.toSeq.flatMap(v => Seq(v._1, v._2, v._3))
  override def dataType: VecType = expr.dataType.asInstanceOf[VecType]
  override def children: Seq[Expr] = Seq(expr) ++ params
  override def resolved: Boolean = {
    children.forall(_.resolved) &&
      expr.dataType.isInstanceOf[VecType] &&
      params.forall(_.dataType == i64)
  }

  override def buildDesc(builder: DescBuilder): Unit = {
    if (startEndStride.isDefined) {
      builder.append("iter(", ",", ")", children)
    } else {
      builder.append(expr)
    }
  }
}

case class For(iters: Seq[Iter], builder: Expr, func: Expr) extends Expr {
  override def children: Seq[Expr] = iters.map(_.expr) :+ builder :+ func

  override def resolved: Boolean = {
    def funcIsValid: Boolean = func match {
      case Lambda(Seq(Identifier(_, builderType), Identifier(_, `i64`), Identifier(_, valueType)), _) =>
        builderType == builder.dataType && valueType == For.valueType(iters)
    }
    iters.nonEmpty &&
      iters.forall(_.resolved) &&
      builder.resolved &&
      func.resolved &&
      builder.dataType == func.dataType &&
      funcIsValid
  }

  override def buildDesc(descBuilder: DescBuilder): Unit = {
    val paramBuilder = descBuilder.appendPrefix("for(")
    if (iters.size > 1) {
      paramBuilder.append("zip(", ", ", ")", iters)
    } else {
      iters.headOption.foreach(paramBuilder.append)
    }
    paramBuilder.append(", ").newLine().append(builder).append(", ").newLine().append(func).append(")")
  }

  override def dataType: WeldType = builder.dataType

  override protected def withNewChildren(children: Seq[Expr]): For = {
    val newVectors :+ newBuilder :+ newFunc = children
    val newIters = newVectors.zip(iters).map {
      case (vector, iter) => iter.copy(expr = vector)
    }
    copy(newIters, newBuilder, newFunc)
  }
}

object For {
  def valueType(iters: Seq[Iter]): WeldType = iters match {
    case Seq() => throw new IllegalArgumentException("No-iters defined.")
    case Seq(iter) => iter.dataType.elementType
    case _ => StructType(iters.map(_.dataType.elementType))
  }
}

case class Merge(left: Expr, right: Expr) extends BinaryExpr with FunctionDesc {
  override def resolved: Boolean = (left.dataType, right.dataType) match {
    case _ if !super.resolved => false
    case (builderType: BuilderType, inputType) => builderType.inputType == inputType
    case _ => false
  }
  override def dataType: WeldType = left.dataType
  override def fn: String = "merge"
  override protected def withNewChildren(left: Expr, right: Expr): Merge = copy(left, right)
}

case class NewBuilder(dataType: BuilderType, intitial: Option[Expr] = None) extends Expr {
  override def resolved: Boolean = dataType.isInstanceOf[BuilderType] && intitial.forall(_.resolved)
  override def children: Seq[Expr] = intitial.toSeq
  override def buildDesc(builder: DescBuilder): Unit = {
    val nestedBuilder = builder.appendPrefix(dataType.name)
    intitial.foreach(i => nestedBuilder.append("(").append(i).append(")"))
  }
  override protected def withNewChildren(children: Seq[Expr]): NewBuilder = {
    val newInitial = children match {
      case Seq(expr) => Some(expr)
      case Seq() => None
    }
    copy(intitial = newInitial)
  }
}

case class Result(child: Expr) extends UnaryExpr with FunctionDesc {
  override def resolved: Boolean = super.resolved && child.dataType.isInstanceOf[BuilderType]
  override def dataType: WeldType = child.dataType.asInstanceOf[BuilderType].outputType
  override def fn: String = "result"
  override protected def withNewChild(newChild: Expr): Result = copy(newChild)
}
