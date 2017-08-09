package weld.expressions

case class Negate(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "-"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(child))
}

case class Exp(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "exp"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(child))
}

case class Log(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "log"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(child))
}

case class Sqrt(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "sqrt"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(child))
}

case class Erf(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "erf"
  override def mapChildren(f: (Expr) => Expr): Expr = copy(f(child))
}
