package weld.expressions

import weld.{UnknownType, WeldType, bool}

abstract class BinOp extends BinaryExpr {
  def symbol: String
  override def children: Seq[Expr] = Seq(left, right)
  override def resolved: Boolean = {
    super.resolved && left.dataType == right.dataType && left.dataType != UnknownType
  }

  override def buildDesc(builder: DescBuilder): Unit = {
    builder.append("(").append(left.desc).append(" ").append(symbol).append(" ").append(right.desc).append(")")
  }

  override def dataType: WeldType = left.dataType
}

case class Add(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "+"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

case class Subtract(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "-"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

case class Multiply(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "*"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

case class Divide(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "/"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

case class Remainder(left: Expr, right: Expr) extends BinOp{
  override def symbol: String = "%"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

case class BitwiseAnd(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "&"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

case class BitwiseOr(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "|"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

case class Xor(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "^"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

abstract class Predicate extends BinOp {
  override def dataType: WeldType = bool
}

case class Equal(left: Expr, right: Expr) extends Predicate{
  override def symbol: String = "=="
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

case class NotEqual(left: Expr, right: Expr) extends Predicate {
  override def symbol: String = "!="
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

case class LessThan(left: Expr, right: Expr) extends Predicate {
  override def symbol: String = "<"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

case class LessThanOrEqual(left: Expr, right: Expr) extends Predicate {
  override def symbol: String = "<="
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

case class GreaterThan(left: Expr, right: Expr) extends Predicate {
  override def symbol: String = ">"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

case class GreaterThanOrEqual(left: Expr, right: Expr) extends Predicate {
  override def symbol: String = ">="
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

abstract class CombinedPredicate extends Predicate {
  override def resolved: Boolean = {
    left.resolved && right.resolved && left.dataType == bool && right.dataType == bool
  }
}
case class LogicalAnd(left: Expr, right: Expr) extends CombinedPredicate {
  override def symbol: String = "&&"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}

case class LogicalOr(left: Expr, right: Expr) extends CombinedPredicate {
  override def symbol: String = "||"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(left), f(right))
}
