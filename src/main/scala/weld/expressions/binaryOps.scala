package weld.expressions

import weld.{UnknownType, WeldType, bool}

abstract class BinOp extends BinaryExpr {
  def symbol: String
  override def children: Seq[Expr] = Seq(left, right)
  override def resolved: Boolean = {
    super.resolved && left.dataType == right.dataType && left.dataType != UnknownType
  }

  override def buildDesc(builder: DescBuilder): Unit = {
    builder.append(left.desc).append(" ").append(symbol).append(" ").append(right.desc)
  }

  override def dataType: WeldType = left.dataType
}

case class Add(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "+"
}

case class Subtract(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "-"
}

case class Multiply(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "*"
}

case class Divide(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "/"
}

case class Modulo(left: Expr, right: Expr) extends BinOp{
  override def symbol: String = "%"
}

case class BitwiseAnd(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "&"
}

case class BitwiseOr(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "|"
}

case class Xor(left: Expr, right: Expr) extends BinOp {
  override def symbol: String = "^"
}

abstract class Predicate extends BinOp {
  override def dataType: WeldType = bool
}

case class Equal(left: Expr, right: Expr) extends Predicate{
  override def symbol: String = "=="
}

case class NotEqual(left: Expr, right: Expr) extends Predicate {
  override def symbol: String = "!="
}

case class LessThan(left: Expr, right: Expr) extends Predicate {
  override def symbol: String = "<"
}

case class LessThanOrEqual(left: Expr, right: Expr) extends Predicate {
  override def symbol: String = "<="
}

case class GreaterThan(left: Expr, right: Expr) extends Predicate {
  override def symbol: String = ">"
}

case class GreaterThanOrEqual(left: Expr, right: Expr) extends Predicate {
  override def symbol: String = ">="
}

abstract class CombinedPredicate extends Predicate {
  override def resolved: Boolean = {
    left.resolved && right.resolved && left.dataType == bool && right.dataType == bool
  }
}
case class LogicalAnd(left: Expr, right: Expr) extends CombinedPredicate {
  override def symbol: String = "&&"
}

case class LogicalOr(left: Expr, right: Expr) extends CombinedPredicate {
  override def symbol: String = "||"
}
