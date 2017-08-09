package weld.expressions

import org.junit.{Assert, Test}
import weld.i64

class ExprTests {
  @Test
  def testBuilding(): Unit = {
    val program = Lambda(Seq(Identifier("a", i64), Identifier("b", i64)), Add(Identifier("a"), Identifier("b")))
    Assert.assertEquals("|a: i64, b: i64| (a + b)" ,program.flatDesc)
  }
}
