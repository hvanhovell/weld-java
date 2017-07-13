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

  @Test
  public void valueTest() {
    try(final WeldValue value = struct(23, -1).toValue()) {
      final WeldStruct struct = value.struct();
      Assert.assertEquals(23, struct.getInt(0));
      Assert.assertEquals(-1, struct.getInt(4));
    }
  }

  @Test
  public void structTest() {
    final WeldStruct input = struct(4, vec(1L, 3L, 4L));
    try (final WeldValue value = input.toValue()) {
      final WeldStruct struct = value.struct();
      Assert.assertEquals(24, struct.size());
      Assert.assertEquals(4, struct.getInt(0));
      final WeldVec vec = struct.getVec(8, 8);
      Assert.assertEquals(3, vec.numElements());
      Assert.assertEquals(24, vec.size());
      Assert.assertEquals(1L, vec.getLong(0));
      Assert.assertEquals(3L, vec.getLong(8));
      Assert.assertEquals(4L, vec.getLong(16));
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
    Assert.assertEquals(12, ivec.size());
    Assert.assertEquals(4, ivec.elementSize());
    Assert.assertEquals(3, ivec.numElements());
    Assert.assertEquals(1, ivec.getInt(0));
    Assert.assertEquals(-3, ivec.getInt(4));
    Assert.assertEquals(8, ivec.getInt(8));

    final WeldStruct fstruct = struct(vec(1.00001f, -35.8f, Float.NaN, Float.NEGATIVE_INFINITY));
    final WeldVec fvec = fstruct.getVec(0, 4);
    Assert.assertEquals(16, fvec.size());
    Assert.assertEquals(4, fvec.elementSize());
    Assert.assertEquals(4, fvec.numElements());
    Assert.assertEquals(1.00001f, fvec.getFloat(0), 1E-9f);
    Assert.assertEquals(-35.8f, fvec.getFloat(4), 1E-9f);
    Assert.assertTrue(Float.isNaN(fvec.getFloat(8)));
    Assert.assertTrue(Float.isInfinite(fvec.getFloat(12)));

    final WeldStruct lstruct = struct(vec(1213091123772781334L, -332479827342344L, 8L, -1L));
    final WeldVec lvec = lstruct.getVec(0, 8);
    Assert.assertEquals(32, lvec.size());
    Assert.assertEquals(8, lvec.elementSize());
    Assert.assertEquals(4, lvec.numElements());
    Assert.assertEquals(lvec.getLong(0), 1213091123772781334L);
    Assert.assertEquals(lvec.getLong(8), -332479827342344L);
    Assert.assertEquals(lvec.getLong(16),8L);
    Assert.assertEquals(lvec.getLong(24), -1L);

    final WeldStruct dstruct = struct(vec(-7384.66d, Double.NaN, -1889.8d));
    final WeldVec dvec = dstruct.getVec(0, 8);
    Assert.assertEquals(24, dvec.size());
    Assert.assertEquals(8, dvec.elementSize());
    Assert.assertEquals(3, dvec.numElements());
    Assert.assertEquals(-7384.66d, dvec.getDouble(0), 1E-9);
    Assert.assertTrue(Double.isNaN(dvec.getDouble(8)));
    Assert.assertEquals(-1889.8d, dvec.getDouble(16), 1E-9);
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
  public void compileAndRun0ArgsScalarRet() {
    String code = "|| i32(0.251 * 4.0)";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue output = module.run(new WeldValue())) {
      final WeldStruct struct = output.result(4);
      Assert.assertEquals(1, struct.getInt(0));
    }
  }

  @Test
  public void compileAndRun1ArgStructRet() {
    String code = "|x:i32| {x + 1, x - 1}";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(42).toValue();
        final WeldValue output = module.run(value)) {
      final WeldStruct struct = output.result(8);
      Assert.assertEquals(43, struct.getInt(0));
      Assert.assertEquals(41, struct.getInt(4));
    }
  }

  @Test
  public void compileAndRun2ArgsVecRet1() {
    String code = "|x:i32, y:i32| [x, y]";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(42, 76).toValue();
        final WeldValue output = module.run(value)) {
      final WeldVec vec = output.result(16).getVec(0,4);
      Assert.assertEquals(42, vec.getInt(0));
      Assert.assertEquals(76, vec.getInt(4));
    }
  }

  @Test
  public void compileAndRun2ArgsVecRet2() {
    String code = "|x:vec[i32], y: i32|result(for(x, appender[i64], |b, i, n| merge(b, i64(y) + i + i64(n) * 2L)))";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(vec(1, 3, 4), 5).toValue();
        final WeldValue output = module.run(value)) {
      final WeldStruct result = output.result(16);
      final WeldVec vec = result.getVec(0,8);
      Assert.assertEquals(7L, vec.getLong(0));
      Assert.assertEquals(12L, vec.getLong(8));
      Assert.assertEquals(15L, vec.getLong(16));
    }
  }

  @Test
  public void compileAndRun2ArgsVecsRet1() {
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
      final WeldVec v1 = result.getVec(0,4);
      final WeldVec v2 = result.getVec(16,8);
      Assert.assertEquals(39, v1.getInt(0));
      Assert.assertEquals(39, v1.getInt(4));
      Assert.assertEquals(39, v1.getInt(8));
      Assert.assertEquals(1L, v2.getLong(0));
      Assert.assertEquals(3L, v2.getLong(8));
      Assert.assertEquals(4L, v2.getLong(16));
    }
  }

  @Test
  public void compileAndRun1ArgVecsRet1() {
    String code =
            "|data:vec[i32]|" +
            "let bs = for(data, {appender[i32], appender[i32]}, |bs, i, n| {merge(bs.$0, n), merge(bs.$1, 2 * n)});" +
            "{result(bs.$0), result(bs.$1)}";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(vec(1, 2, 3)).toValue();
        final WeldValue output = module.run(value)) {
      final WeldStruct result = output.result(32);
      final WeldVec vec1 = result.getVec(0,4);
      Assert.assertEquals(3, vec1.numElements());
      Assert.assertEquals(1, vec1.getInt(0));
      Assert.assertEquals(2, vec1.getInt(4));
      Assert.assertEquals(3, vec1.getInt(8));
      final WeldVec vec2 = result.getVec(16,4);
      Assert.assertEquals(3, vec2.numElements());
      Assert.assertEquals(2, vec2.getInt(0));
      Assert.assertEquals(4, vec2.getInt(4));
      Assert.assertEquals(6, vec2.getInt(8));
    }
  }

  @Test
  public void compileAndRun1ArgsStructRet1() {
    String code =
            "|ys:vec[i64]|" +
            "let r = result(for(ys, appender[{i64, i32}], |b0, i0, y0| for(ys, b0, |b1, i1, y1| if (y1 > y0, merge(b0, {y0, i32(y1)}), b0))));" +
            "{r, 53}";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(vec(1L, 3L, 4L)).toValue();
        final WeldValue output = module.run(value)) {
      final WeldStruct result = output.result(20);
      Assert.assertEquals(53, result.getInt(16));
      final WeldVec vec = result.getVec(0, 16);
      Assert.assertEquals(3, vec.numElements());
      Assert.assertEquals(1L, vec.getStruct(0).getLong(0));
      Assert.assertEquals(3, vec.getStruct(0).getInt(8));
      Assert.assertEquals(1L, vec.getStruct(16).getLong(0));
      Assert.assertEquals(4, vec.getStruct(16).getInt(8));
      Assert.assertEquals(3L, vec.getStruct(32).getLong(0));
      Assert.assertEquals(4, vec.getStruct(32).getInt(8));
    }
  }
}
