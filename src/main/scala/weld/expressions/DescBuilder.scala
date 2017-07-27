package weld.expressions

trait DescBuilder {
  def builder: StringBuilder

  def appendPrefix(p: String): DescBuilder = {
    newBuilder(p.length).append(p)
  }

  def append(b: String, sep: String, e: String, exprs: Seq[ExprLike]): DescBuilder = {
    val newBuilder = appendPrefix(b)
    var first = true
    exprs.foreach { expr =>
      if (!first) {
        newBuilder.append(sep)
      }
      newBuilder.append(expr)
      first = false
    }
    newBuilder.append(e)
  }

  def append(e: ExprLike): DescBuilder = {
    e.buildDesc(this)
    this
  }

  def append(s: String): DescBuilder = {
    builder.append(s)
    this
  }

  def newLine(increment: Int = 0): DescBuilder

  def newBuilder(increment: Int): DescBuilder

  def desc: String = builder.toString()
}


case class IndentedDescBuilder(builder: StringBuilder = new StringBuilder, indent: Int = 0) extends DescBuilder {
  override def newBuilder(increment: Int): DescBuilder = copy(indent = indent + increment)
  override def newLine(increment: Int): DescBuilder = newBuilder(increment).append("\n").append(" " * (indent + increment))
}

case class SimpleDescBuilder(builder: StringBuilder) extends DescBuilder {
  override def newBuilder(increment: Int): DescBuilder = this
  override def newLine(increment: Int): DescBuilder = append(" ")
}
