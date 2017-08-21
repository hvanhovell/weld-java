package weld.expressions

case class Negate(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "-"
  override protected def withNewChild(newChild: Expr): Negate = copy(newChild)
}

case class Exp(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "exp"
  override protected def withNewChild(newChild: Expr): Exp = copy(newChild)
}

case class Log(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "log"
  override protected def withNewChild(newChild: Expr): Log = copy(newChild)
}

case class Sqrt(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "sqrt"
  override protected def withNewChild(newChild: Expr): Sqrt = copy(newChild)
}

case class Erf(child: Expr) extends UnaryExpr with FunctionDesc {
  override def fn: String = "erf"
  override protected def withNewChild(newChild: Expr): Erf = copy(newChild)
}
