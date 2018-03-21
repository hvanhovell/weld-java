package weld.expressions

import org.junit.{Assert, Test}
import weld.WeldStruct.struct
import weld._
import weld.expressions.DSL._

class ExprTests {
  @Test
  def testBuilding(): Unit = {
    val program = Lambda(Seq(Identifier("a", i64), Identifier("b", i64)), Add(Identifier("a"), Identifier("b")))
    Assert.assertEquals("|a: i64, b: i64| (a + b)", program.flatDesc)
  }

  @Test
  def testRangeIter(): Unit = {
    def program = lambda() { ctx =>
      import ctx._
      val loop = let("loop", fore(zip(rangeIter(0L, 10L, 1L)), appender(i64)) {
        (_, b, _, n) => merge(b, n + 1L)
      })
      result(loop)
    }
    Resources.withCleanup { track =>
      val module = track(WeldModule.compile(program.flatDesc))
      val input = track(WeldValue.empty())
      val output = track(module.run(input))
        .result(VecType.vecOf(i64))
        .getVec(0)
      assert(output.numElements == 10)
      assert(output.getLong(0) == 1L)
    }
  }

  @Test
  def testSort(): Unit = {
    def program = {
      val input = "input".vec(i32)
      lambda(input) { ctx =>
        ctx.sort(input)((_, n) => -n)
      }
    }
    Resources.withCleanup { track =>
      val module = track(WeldModule.compile(program.flatDesc))
      val input = track(struct(WeldVec.vec(Array(1, 4, 3))).toValue)
      val output = track(module.run(input))
        .result(VecType.vecOf(i32))
        .getVec(0)
      assert(output.numElements == 3)
      assert(output.getInt(0) == 4)
      assert(output.getInt(1) == 3)
      assert(output.getInt(2) == 1)
    }
  }
}
