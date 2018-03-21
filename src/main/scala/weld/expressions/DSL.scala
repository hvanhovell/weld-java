package weld.expressions

import weld._

import scala.collection.mutable

object DSL {
  trait IdentifierConversions {
    val id: Identifier
    def bool: Identifier = id.copy(dataType = weld.bool)
    def u8: Identifier = id.copy(dataType = weld.u8)
    def i8: Identifier = id.copy(dataType = weld.i8)
    def u16: Identifier = id.copy(dataType = weld.u16)
    def i16: Identifier = id.copy(dataType = weld.i16)
    def u32: Identifier = id.copy(dataType = weld.u32)
    def i32: Identifier = id.copy(dataType = weld.i32)
    def u64: Identifier = id.copy(dataType = weld.u64)
    def i64: Identifier = id.copy(dataType = weld.i64)
    def f32: Identifier = id.copy(dataType = weld.f32)
    def f64: Identifier = id.copy(dataType = weld.f64)
    def vec(dt: WeldType): Identifier = id.copy(dataType = VecType(dt))
    def struct(dts: WeldType*): Identifier = id.copy(dataType = StructType(dts))
  }

  trait ExprOperations {
    val e: Expr
    def unary_- : Expr = Negate(e)
    def +(r: Expr): Expr = Add(e, r)
    def -(r: Expr): Expr = Subtract(e, r)
    def *(r: Expr): Expr = Multiply(e, r)
    def /(r: Expr): Expr = Divide(e, r)
    def %(r: Expr): Expr = Remainder(e, r)
    def &(r: Expr): Expr = BitwiseAnd(e, r)
    def |(r: Expr): Expr = BitwiseOr(e, r)
    def ^(r: Expr): Expr = Xor(e, r)
    def ===(r: Expr): Expr = Equal(e, r)
    def =!=(r: Expr): Expr = NotEqual(e, r)
    def <(r: Expr): Expr = LessThan(e, r)
    def <=(r: Expr): Expr = LessThanOrEqual(e, r)
    def >(r: Expr): Expr = GreaterThan(e, r)
    def >=(r: Expr): Expr = GreaterThanOrEqual(e, r)
    def &&(r: Expr): Expr = LogicalAnd(e, r)
    def ||(r: Expr): Expr = LogicalOr(e, r)
    def cast(pt: PrimitiveType): Expr = Cast(e, pt)
    def broadcast: Expr = Broadcast(e)
    def toVec: Expr = ToVec(e)
    def apply(i: Int): Expr = getField(i)
    def getField(i: Int): Expr = GetField(e, i)
    def apply(r: Expr): Expr = lookup(r)
    def lookup(r: Expr): Expr = Lookup(e, r)
    def length: Expr = Length(e)
    def keyExists(r: Expr): Expr = KeyExists(e, r)
    def slice(offset: Expr, length: Expr): Expr = Slice(e, offset, length)
    def serialize: Expr = Serialize(e)
    def deserialize(weldType: WeldType): Expr = Deserialize(weldType, e)
    def merge(r: Expr): Expr = Merge(e, r)
    def result(): Expr = Result(e)
  }

  implicit class RichName(val name: String) extends IdentifierConversions with ExprOperations {
    override val id: Identifier = Identifier(name, UnknownType)
    override val e: Expr = id
  }
  implicit class RichExpr(val e: Expr) extends ExprOperations

  implicit def booleanToLiteral(b: Boolean): Expr = Literal(b)
  implicit def byteToLiteral(b: Byte): Expr = Literal(b)
  implicit def shortToLiteral(s: Short): Expr = Literal(s)
  implicit def intToLiteral(i: Int): Expr = Literal(i)
  implicit def longToLiteral(l: Long): Expr = Literal(l)
  implicit def floatToLiteral(f: Float): Expr = Literal(f)
  implicit def doubleToLiteral(d: Double): Expr = Literal(d)
  implicit def stringToLiteral(s: String): Expr = Literal(s)
  implicit def bytesToLiteral(b: Array[Byte]): Expr = Literal(b)

  def lit(value: Any): Expr = Literal(value)
  def exp(e: Expr): Expr = Exp(e)
  def log(e: Expr): Expr = Log(e)
  def sqrt(e: Expr): Expr = Sqrt(e)
  def erf(e: Expr): Expr = Erf(e)
  def sin(e: Expr): Expr = Sin(e)
  def asin(e: Expr): Expr = ASin(e)
  def sinh(e: Expr): Expr = SinH(e)
  def cos(e: Expr): Expr = Cos(e)
  def acos(e: Expr): Expr = ACos(e)
  def cosh(e: Expr): Expr = CosH(e)
  def tan(e: Expr): Expr = Tan(e)
  def atan(e: Expr): Expr = ATan(e)
  def tanh(e: Expr): Expr = TanH(e)
  def iif(cond: Expr, onTrue: Expr, onFalse: Expr): Expr = If(cond, onTrue, onFalse)
  def select(cond: Expr, onTrue: Expr, onFalse: Expr): Expr = Select(cond, onTrue, onFalse)
  def struct(es: Expr*): Expr = MakeStruct(es)
  def vec(es: Expr*): Expr = MakeVector(es)
  def cudf(name: String, weldType: WeldType, es: Expr*): Expr = CUDF(name, es, weldType)
  def serialize(e: Expr): Expr = Serialize(e)
  def deserialize(e: Expr, weldType: WeldType): Expr = Deserialize(weldType, e)
  def merge(e: Expr, r: Expr): Expr = Merge(e, r)
  def result(e: Expr): Expr = Result(e)
  def appender(weldType: WeldType): Expr = NewBuilder(Appender(weldType))
  def appender(weldType: WeldType, init: Expr): Expr = NewBuilder(Appender(weldType), Option(init))
  def zip(vectors: Expr*): Seq[Expr] = vectors
  def rangeIter(start: Expr, stop: Expr, stride: Expr): Expr = RangeIter(start, stop, stride)
  def lambda(ids: Identifier*)(f: LambdaBuilder => Expr): Expr = {
    val generator = new UniqueIdentifierGenerator(predefined = ids.map(_.name))
    val builder = new LambdaBuilder(ids, generator)
    builder.build(f)
  }

  /**
   * Helper class for creating weld lambda functions.
   */
  class LambdaBuilder(ids: Seq[Identifier], generator: IdentifierGenerator) {
    private val namedExprs = mutable.ArrayBuffer.empty[(Identifier, Expr)]

    /**
     * Assign the value of an expression to a local variable.
     */
    def let(name: String, value: Expr): Identifier = {
      val id = generator(name, value.dataType)
      namedExprs += id -> value
      id
    }

    /**
     * Build a nested [[For]] expression.
     */
    def fore(
        vectors: Seq[Expr],
        builder: Expr)(
        f: (LambdaBuilder, Identifier, Identifier, Identifier) => Expr): Expr = {
      val iters = vectors.map {
        case i: Iter => i
        case e => ScalarIter(e)
      }
      val gen = generator.createScopedGenerator()
      val b = gen("b", builder.dataType)
      val i = gen("i", i64)
      val n = gen("n", For.valueType(iters))
      val lambdaBuilder = new LambdaBuilder(Seq(b, i, n), gen)
      val lastExpr = f(lambdaBuilder, b, i, n)
      For(iters, builder, lambdaBuilder.build(lastExpr))
    }

    /**
     * Build a nested [[Iterate]] expression.
     */
    def iterate(e: Expr)(f: (LambdaBuilder, Identifier) => Expr): Expr = {
      val gen = generator.createScopedGenerator()
      val n = gen("n", e.dataType)
      val lambdaBuilder = new LambdaBuilder(Seq(n), gen)
      val lastExpr = f(lambdaBuilder, n)
      Iterate(e, lambdaBuilder.build(lastExpr))
    }

    /**
     * Build a nested [[Sort]] expression.
     */
    def sort(e: Expr)(f: (LambdaBuilder, Identifier) => Expr): Expr = {
      val gen = generator.createScopedGenerator()
      val n = gen("n", e.dataType.asInstanceOf[VecType].elementType)
      val lambdaBuilder = new LambdaBuilder(Seq(n), gen)
      val lastExpr = f(lambdaBuilder, n)
      Sort(e, lambdaBuilder.build(lastExpr))
    }

    /**
     * Create the lambda expression from a generating function.
     */
    def build(f: LambdaBuilder => Expr): Lambda = build(f(this))

    /**
     * Create the lambda expression.
     */
    def build(e: Expr): Lambda = {
      val body = namedExprs.foldRight(e) {
        case ((id, value), b) =>
          Let(id.name, value, b)
      }
      Lambda(ids, body)
    }
  }

  trait IdentifierGenerator extends ((String, WeldType) => Identifier) {
    def createScopedGenerator(): IdentifierGenerator
  }

  class UniqueIdentifierGenerator(
      parent: Option[UniqueIdentifierGenerator] = None,
      predefined: Iterable[String] = Set.empty)
    extends IdentifierGenerator {

    private val names = {
      val map = mutable.Map.empty[String, Int]
      predefined.foreach(map.put(_, 0))
      map
    }

    /**
     * Get the next index for a name.
     */
    protected def nameIndex(name: String): Int = {
      names.getOrElseUpdate(name, parent.map(_.nameIndex(name)).getOrElse(0))
    }

    /**
     * Create a hierarchical scoped namer.
     */
    override def createScopedGenerator(): IdentifierGenerator = {
      new UniqueIdentifierGenerator(Some(this))
    }

    /**
     * Generate identifier with a unique name.
     */
    override def apply(name: String, weldType: WeldType): Identifier = {
      val index = nameIndex(name)
      names.put(name, index + 1)
      val uniqueName = if (index == 0) name else name + index
      Identifier(uniqueName, weldType)
    }
  }
}
