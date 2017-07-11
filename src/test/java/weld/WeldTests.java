package weld;

import org.junit.*;
import org.junit.Assert;

import static weld.WeldStruct.struct;
import static weld.WeldVec.vec;

/**
 * Tests for Weld-Java integration.
 */
public class WeldTests {
  /**
   * Test the conf class.
   */
  @Test
  public void confTest() {
    try(final WeldConf conf = new WeldConf()) {
      // Non existing key.
      Assert.assertNull(conf.get("x.y"));

      // Existing key.
      conf.set("x.y", "test");
      Assert.assertEquals("test", conf.get("x.y"));
    }
  }

  /**
   * Test the error class.
   */
  @Test
  public void errorTest() {
    try(final WeldError error = new WeldError()) {
      Assert.assertEquals(0, error.getCode());
      Assert.assertEquals("Success", error.getMessage());
    }
  }

  /**
   * Test the value class. Note that this is a bit of a no-op since we cannot really get the values
   * out unless we use sun.misc.Unsafe.
   */
  @Test
  public void valueTest() {
    try(final WeldValue value = struct(23, -1).toValue()) {
      final WeldStruct struct = value.struct();
      Assert.assertEquals(23, struct.getInt(0));
      Assert.assertEquals(-1, struct.getInt(4));
    }
  }

  @Test
  public void vecTest() {
    final WeldStruct bitstruct = struct(vec(true, true, false));
    final WeldVec bitvec = bitstruct.getVec(0, 1);
    Assert.assertEquals(3, bitvec.size());
    Assert.assertEquals(1, bitvec.elementSize());
    Assert.assertEquals(3, bitvec.numElements());
    Assert.assertEquals(true, bitvec.getBoolean(0));
    Assert.assertEquals(true, bitvec.getBoolean(1));
    Assert.assertEquals(false, bitvec.getBoolean(2));

    final WeldStruct bstruct = struct(vec((byte) -1, (byte) 88, (byte) -127));
    final WeldVec bvec = bstruct.getVec(0, 1);
    Assert.assertEquals(3, bvec.size());
    Assert.assertEquals(1, bvec.elementSize());
    Assert.assertEquals(3, bvec.numElements());
    Assert.assertEquals((byte) -1, bvec.getByte(0));
    Assert.assertEquals((byte) 88, bvec.getByte(1));
    Assert.assertEquals((byte) -127, bvec.getByte(2));

    final WeldStruct istruct = struct(vec(1, -3, 8));
    final WeldVec ivec = istruct.getVec(0, 4);
    Assert.assertEquals(ivec.size(), 12);
    Assert.assertEquals(ivec.elementSize(), 4);
    Assert.assertEquals(ivec.numElements(), 3);
    Assert.assertEquals(ivec.getInt(0), 1);
    Assert.assertEquals(ivec.getInt(4), -3);
    Assert.assertEquals(ivec.getInt(8), 8);

    final WeldStruct lstruct = struct(vec(1213091123772781334L, -332479827342344L, 8L, -1L));
    final WeldVec lvec = lstruct.getVec(0, 8);
    Assert.assertEquals(32, lvec.size());
    Assert.assertEquals(8, lvec.elementSize());
    Assert.assertEquals(4, lvec.numElements());
    Assert.assertEquals(1213091123772781334L, lvec.getLong(0));
    Assert.assertEquals(-332479827342344L, lvec.getLong(8));
    Assert.assertEquals(8L, lvec.getLong(16));
    Assert.assertEquals(-1L, lvec.getLong(24));

    final WeldStruct dstruct = struct(vec(-7384.66d, Double.NaN, -1889.8d));
    final WeldVec dvec = dstruct.getVec(0, 8);
    Assert.assertEquals(24, dvec.size());
    Assert.assertEquals(8, dvec.elementSize());
    Assert.assertEquals(3, dvec.numElements());
    // Assert.assertEquals(Double.doubleToLongBits(-7384.66d), dvec.getLong(0));
    // Assert.assertTrue(Double.isNaN(dvec.getDouble(8)));
    //Assert.assertEquals(-1889.8d, dvec.getDouble(16), 1E-9);
  }

  /**
   * Test compilation failure.
   */
  @Test
  public void compileTestFail() {
    String code = "foo";
    try {
      WeldModule.compile(code);
      Assert.fail("Compilation should have failed.");
    } catch(final WeldException e) {
      Assert.assertEquals(e.getCode(), 1);
      Assert.assertEquals(e.getMessage(), "Undefined symbol foo in uniquify");
    }
  }

  @Test
  public void crSimpleTest() {
    String code = "|| i32(0.251 * 4.0)";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue output = module.run(new WeldValue())) {
      final WeldStruct struct = output.result(4);
      Assert.assertEquals(struct.getInt(0), 1);
    }
  }

  @Test
  public void crSingleArgumentTest() {
    String code = "|x:i32| {x + 1, x - 1}";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(42).toValue();
        final WeldValue output = module.run(value)) {
          final WeldStruct struct = output.result(8);
          Assert.assertEquals(struct.getInt(0), 43);
          Assert.assertEquals(struct.getInt(4), 41);
    }
  }

  @Test
  public void crTwoArgumentTest() {
    String code = "|x:i32, y:i32| [x, y]";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(42, 76).toValue();
        final WeldValue output = module.run(value)) {
      final WeldVec vec = output.result(16).getVec(0,4);
      Assert.assertEquals(vec.getInt(0), 42);
      Assert.assertEquals(vec.getInt(4), 76);
    }
  }

  @Ignore
  public void crComplex1() {
    // This is a problem on the Weld side.
    // I suspect that the 'ys' iterator is reused by both paths. This also implies that Weld is
    // computing rows twice (!!!).
    String code =
            "|x:i32, ys:vec[i64]|" +
            "let rows = result(for(ys, appender[{i32, i64}], |b, i, y| merge(b, {x, y})));" +
            "let a = result(for(rows, appender[i32], |b, i, n| merge(b, n.$0)));" +
            "let b = result(for(rows, appender[i64], |b, i, n| merge(b, n.$1)));" +
            "{a, b}";
    final WeldStruct struct = struct(39, vec(1L, 3L, 4L));
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct.toValue();
        final WeldValue output = module.run(value)) {
      final WeldStruct result = output.result(32);
      final WeldVec v1 = result.getVec(0,8);
      final WeldVec v2 = result.getVec(16,8);
      Assert.assertEquals(v1.getInt(0), 39);
      Assert.assertEquals(v1.getInt(4), 39);
      Assert.assertEquals(v1.getInt(8), 39);
      Assert.assertEquals(v2.getLong(0), 1);
      Assert.assertEquals(v2.getLong(8), 3);
      Assert.assertEquals(v2.getLong(16), 4);
    }
  }

  @Test
  public void crComplex2() {
    String code = "|x:vec[i64], y: i32|result(for(x, appender[i64], |b, i, n| merge(b, i64(y) + i + n * 2L)))";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(vec(1L, 3L, 4L), 5).toValue();
        final WeldValue output = module.run(value)) {
      final WeldStruct result = output.result(16);
      final WeldVec vec = result.getVec(0,8);
      Assert.assertEquals(vec.getLong(0), 7L);
      Assert.assertEquals(vec.getLong(8), 12L);
      Assert.assertEquals(vec.getLong(16), 15L);
    }
  }

  @Ignore
  public void crComplex3() {
    // This is a problem on the Weld side, see: https://github.com/weld-project/weld/issues/161
    String code =
            "||" +
            "let b0 = appender[i32];" +
            "let b1 = appender[i32];" +
            "let data = [1, 2, 3];" +
            "let bs = for(" +
            "  data," +
            "  {b0, b1}," +
            "  |bs: {appender[i32], appender[i32]}, i: i64, n: i32| {merge(bs.$0, n), merge(bs.$1, 2 * n)}" +
            ");" +
            "result(bs)";

    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = new WeldValue();
        final WeldValue output = module.run(value)) {
      final WeldStruct result = output.result(32);
      final WeldVec vec1 = result.getVec(0,4);
      final WeldVec vec2 = result.getVec(16,4);
      Assert.assertEquals(vec1.getInt(0), 1);
    }
  }

  @Ignore
  public void crComplex4() {
    String code =
            "|ys:vec[i64]|" +
            "let r = result(for(ys, appender[{i64, i64}], |b0, i0, y0| for(ys, b0, |b1, i1, y1| if (y1 >= y0, merge(b0, {y0, y1}), b0))));" +
            "{r, 53}";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(vec(1L, 3L, 4L)).toValue();
        final WeldValue output = module.run(value)) {
      final WeldStruct result = output.result(20);
      Assert.assertEquals(53, result.getInt(16));
      final WeldVec vec = result.getVec(0,8);
      Assert.assertEquals(6, vec.numElements());
      // TODO
    }
  }
}
