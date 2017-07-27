package weld.expressions

case class Negate(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "-"
}

case class Exp(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "exp"
}

case class Log(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "log"
}

case class Sqrt(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "sqrt"
}

case class Erf(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "erf"
}
