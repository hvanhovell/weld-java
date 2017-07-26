package weld;

import org.junit.*;
import org.junit.Assert;

import java.util.concurrent.*;

import static weld.StructType.structOf;
import static weld.WeldStruct.struct;
import static weld.VecType.vecOf;

/**
 * Tests for Weld-Java integration.
 */
public class WeldTests {
  private WeldType i8 = i8$.MODULE$;
  private WeldType i32 = i32$.MODULE$;
  private WeldType i64 = i64$.MODULE$;
  private WeldType pointer = Pointer$.MODULE$;

  private WeldVec.Builder vec(boolean... values) {
    return WeldVec.vec(values);
  }
  private WeldVec.Builder vec(byte... values) {
    return WeldVec.vec(values);
  }
  private WeldVec.Builder vec(int... values) {
    return WeldVec.vec(values);
  }
  private WeldVec.Builder vec(long... values) {
    return WeldVec.vec(values);
  }
  private WeldVec.Builder vec(float... values) {
    return WeldVec.vec(values);
  }
  private WeldVec.Builder vec(double... values) {
    return WeldVec.vec(values);
  }

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
      Assert.assertEquals(0, error.code());
      Assert.assertEquals("Success", error.message());
    }
  }

  @Test
  public void valueTest() {
    try(final WeldValue value = struct(23, -1).toValue()) {
      final WeldStruct struct = value.result(i32, i32);
      Assert.assertEquals(23, struct.getInt(0));
      Assert.assertEquals(-1, struct.getInt(1));
    }
  }

  @Test
  public void closeIdempotentTest() {
    // Make sure close is idempotent, and does not throw horrific errors.
    final WeldValue value = struct(23, -1).toValue();
    value.close();
    value.close();
  }

  @Test(expected = AssertionError.class)
  public void useAfterCloseTest() {
    final WeldValue value = struct(23, -1).toValue();
    value.close();
    value.getPointer();
  }

  @Test
  public void structTest() {
    final WeldStruct input = struct(4, vec(1L, 3L, 4L));
    try (final WeldValue value = input.toValue()) {
      final WeldStruct struct = value.result(i32, vecOf(i64));
      Assert.assertEquals(24, struct.size());
      Assert.assertEquals(4, struct.getInt(0));
      final WeldVec vec = struct.getVec(1);
      Assert.assertEquals(3, vec.numElements());
      Assert.assertEquals(24, vec.size());
      Assert.assertEquals(1L, vec.getLong(0));
      Assert.assertEquals(3L, vec.getLong(1));
      Assert.assertEquals(4L, vec.getLong(2));
    }
  }

  @Test
  public void vec2Test() {
    // Collectively allocated vectors & struct.
    try (final WeldStruct input = struct(42, vec(1), vec((byte) 0))) {
      Assert.assertEquals(40, input.size());
    }

    // Separately allocated vectors & struct.
    try (final WeldStruct s1 = struct(vec(1));
         final WeldStruct s2 = struct(vec((byte) 0));
         final WeldStruct input = struct(42, s1.getVec(0), s2.getVec(0))) {
      Assert.assertEquals(40, input.size());
    }
  }

  @Test
  public void vecTest() {
    final WeldStruct bitstruct = struct(vec(true, true, false));
    final WeldVec bitvec = bitstruct.getVec(0);
    Assert.assertEquals(3, bitvec.size());
    Assert.assertEquals(1, bitvec.elementSize());
    Assert.assertEquals(3, bitvec.numElements());
    Assert.assertEquals(true, bitvec.getBoolean(0));
    Assert.assertEquals(true, bitvec.getBoolean(1));
    Assert.assertEquals(false, bitvec.getBoolean(2));

    final WeldStruct bstruct = struct(vec((byte) -1, (byte) 88, (byte) -127));
    final WeldVec bvec = bstruct.getVec(0);
    Assert.assertEquals(3, bvec.size());
    Assert.assertEquals(1, bvec.elementSize());
    Assert.assertEquals(3, bvec.numElements());
    Assert.assertEquals((byte) -1, bvec.getByte(0));
    Assert.assertEquals((byte) 88, bvec.getByte(1));
    Assert.assertEquals((byte) -127, bvec.getByte(2));

    final WeldStruct istruct = struct(vec(1, -3, 8));
    final WeldVec ivec = istruct.getVec(0);
    Assert.assertEquals(12, ivec.size());
    Assert.assertEquals(4, ivec.elementSize());
    Assert.assertEquals(3, ivec.numElements());
    Assert.assertEquals(1, ivec.getInt(0));
    Assert.assertEquals(-3, ivec.getInt(1));
    Assert.assertEquals(8, ivec.getInt(2));

    final WeldStruct fstruct = struct(vec(1.00001f, -35.8f, Float.NaN, Float.NEGATIVE_INFINITY));
    final WeldVec fvec = fstruct.getVec(0);
    Assert.assertEquals(16, fvec.size());
    Assert.assertEquals(4, fvec.elementSize());
    Assert.assertEquals(4, fvec.numElements());
    Assert.assertEquals(1.00001f, fvec.getFloat(0), 1E-9f);
    Assert.assertEquals(-35.8f, fvec.getFloat(1), 1E-9f);
    Assert.assertTrue(Float.isNaN(fvec.getFloat(2)));
    Assert.assertTrue(Float.isInfinite(fvec.getFloat(3)));

    final WeldStruct lstruct = struct(vec(1213091123772781334L, -332479827342344L, 8L, -1L));
    final WeldVec lvec = lstruct.getVec(0);
    Assert.assertEquals(32, lvec.size());
    Assert.assertEquals(8, lvec.elementSize());
    Assert.assertEquals(4, lvec.numElements());
    Assert.assertEquals(lvec.getLong(0), 1213091123772781334L);
    Assert.assertEquals(lvec.getLong(1), -332479827342344L);
    Assert.assertEquals(lvec.getLong(2),8L);
    Assert.assertEquals(lvec.getLong(3), -1L);

    final WeldStruct dstruct = struct(vec(-7384.66d, Double.NaN, -1889.8d));
    final WeldVec dvec = dstruct.getVec(0);
    Assert.assertEquals(24, dvec.size());
    Assert.assertEquals(8, dvec.elementSize());
    Assert.assertEquals(3, dvec.numElements());
    Assert.assertEquals(-7384.66d, dvec.getDouble(0), 1E-9);
    Assert.assertTrue(Double.isNaN(dvec.getDouble(1)));
    Assert.assertEquals(-1889.8d, dvec.getDouble(2), 1E-9);
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
      Assert.assertEquals(e.code(), 3);
      Assert.assertEquals(e.getMessage(), "Undefined symbol foo in uniquify");
    }
  }

  @Test
  public void compileAndRun0ArgsScalarRet() {
    String code = "|| i32(0.251 * 4.0)";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue output = module.run(WeldValue.empty())) {
      final WeldStruct struct = output.result(i32);
      Assert.assertEquals(1, struct.getInt(0));
    }
  }

  @Test
  public void compileAndRun1ArgStructRet() {
    String code = "|x:i32| {x + 1, x - 1}";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(42).toValue();
        final WeldValue output = module.run(value)) {
      final WeldStruct struct = output.result(i32, i32);
      Assert.assertEquals(43, struct.getInt(0));
      Assert.assertEquals(41, struct.getInt(1));
    }
  }

  @Test
  public void compileAndRun2ArgsVecRet1() {
    String code = "|x:i32, y:i32| [x, y]";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(42, 76).toValue();
        final WeldValue output = module.run(value)) {
      final WeldVec vec = output.result(vecOf(i32)).getVec(0);
      Assert.assertEquals(42, vec.getInt(0));
      Assert.assertEquals(76, vec.getInt(1));
    }
  }

  @Test
  public void compileAndRun2ArgsVecRet2() {
    doCompileAndRun2ArgsVecRet2();
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
      final WeldStruct result = output.result(vecOf(i32), vecOf(i64));
      final WeldVec v1 = result.getVec(0);
      final WeldVec v2 = result.getVec(1);
      Assert.assertEquals(39, v1.getInt(0));
      Assert.assertEquals(39, v1.getInt(1));
      Assert.assertEquals(39, v1.getInt(2));
      Assert.assertEquals(1L, v2.getLong(0));
      Assert.assertEquals(3L, v2.getLong(1));
      Assert.assertEquals(4L, v2.getLong(2));
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
      final WeldStruct result = output.result(vecOf(i32), vecOf(i32));
      final WeldVec vec1 = result.getVec(0);
      Assert.assertEquals(3, vec1.numElements());
      Assert.assertEquals(1, vec1.getInt(0));
      Assert.assertEquals(2, vec1.getInt(1));
      Assert.assertEquals(3, vec1.getInt(2));
      final WeldVec vec2 = result.getVec(1);
      Assert.assertEquals(3, vec2.numElements());
      Assert.assertEquals(2, vec2.getInt(0));
      Assert.assertEquals(4, vec2.getInt(1));
      Assert.assertEquals(6, vec2.getInt(2));
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
      final WeldStruct result = output.result(vecOf(structOf(i64, i32)), i32);
      Assert.assertEquals(53, result.getInt(1));
      final WeldVec vec = result.getVec(0);
      Assert.assertEquals(3, vec.numElements());
      Assert.assertEquals(1L, vec.getStruct(0).getLong(0));
      Assert.assertEquals(3, vec.getStruct(0).getInt(1));
      Assert.assertEquals(1L, vec.getStruct(1).getLong(0));
      Assert.assertEquals(4, vec.getStruct(1).getInt(1));
      Assert.assertEquals(3L, vec.getStruct(2).getLong(0));
      Assert.assertEquals(4, vec.getStruct(2).getInt(1));
    }
  }

  @Test
  public void compileAndRun1ArgsVecsRet1() {
    String code = "|ys:vec[i64]| result(for(ys, appender[{i64, i8, i32}], |b, i, y| merge(b, {y, i8(y), i32(i)})))";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(vec(1L, 99L)).toValue();
        final WeldValue output = module.run(value)) {
      final WeldStruct result = output.result(vecOf(structOf(i64, i8, i32)));
      final WeldVec vec = result.getVec(0);
      Assert.assertEquals(2, vec.numElements());
      Assert.assertEquals(1L, vec.getStruct(0).getLong(0));
      Assert.assertEquals((byte) 1, vec.getStruct(0).getByte(1));
      Assert.assertEquals(0, vec.getStruct(0).getInt(2));
      Assert.assertEquals(99L, vec.getStruct(1).getLong(0));
      Assert.assertEquals((byte) 99, vec.getStruct(1).getByte(1));
      Assert.assertEquals(1, vec.getStruct(1).getInt(2));
    }
  }

  @Test
  public void compileAndRunConcurrent() {
    final int threads = Runtime.getRuntime().availableProcessors() + 1;
    final ExecutorService executor = Executors.newFixedThreadPool(threads);
    try {
      for (int i = 0; i < threads * 2; i++) {
        executor.submit(this::doCompileAndRun2ArgsVecRet2);
      }
    } finally {
      executor.shutdown();
    }
  }

  private void doCompileAndRun2ArgsVecRet2() {
    String code = "|x:vec[i32], y: i32|result(for(x, appender[i64], |b, i, n| merge(b, i64(y) + i + i64(n) * 2L)))";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(vec(1, 3, 4), 5).toValue();
        final WeldValue output = module.run(value)) {
      final WeldStruct result = output.result(vecOf(i64));
      final WeldVec vec = result.getVec(0);
      Assert.assertEquals(7L, vec.getLong(0));
      Assert.assertEquals(12L, vec.getLong(1));
      Assert.assertEquals(15L, vec.getLong(2));
    }
  }

  @Test
  public void compileAndRunSerialPrograms() {
    String code1 = "|x: vec[i32], y: i32|result(for(x, appender[i64], |b, i, n| merge(b, i64(y) + i + i64(n) * 2L)))";
    String code2 = "|z: vec[i64]| result(for(z, merger[i64, +], |b, i, n| merge(b, n)))";
    try(final WeldModule module1 = WeldModule.compile(code1);
        final WeldModule module2 = WeldModule.compile(code2);
        final WeldValue value = struct(vec(1, 3, 4), 5).toValue();
        final WeldValue intermediate = module1.run(value);
        final WeldValue output = module2.run(intermediate)) {
      final WeldStruct result = output.result(i64);
      Assert.assertEquals(34L, result.getLong(0));
    }
  }

  @Test
  public void compileAndRunPassBuilder1() {
    String initializeCode = "||merger[i64, +]";
    String updateCode = "|m: merger[i64, +], x: vec[i64]| for(x, m, |b, i, n| merge(b, n))";
    String finalizeCode = "|m: merger[i64, +]| result(m)";
    try(final WeldModule initialize = WeldModule.compile(initializeCode);
        final WeldModule update = WeldModule.compile(updateCode);
        final WeldModule finalize = WeldModule.compile(finalizeCode);
        final WeldValue empty = WeldValue.empty();
        final WeldValue buffer = initialize.run(empty)) {
      // Get the buffer pointer.
      long address = buffer.result(pointer).getPointer(0);

      // Update the buffer in 10 increments.
      long expected = 0;
      for (int i = 0; i < 10; i++) {
        try (final WeldStruct arguments = struct(address, vec(1L + i, 2L + i, 3L + i));
             final WeldValue input = arguments.toValue()) {
          final WeldValue output = update.run(input);
          Assert.assertEquals(address, output.result(pointer).getPointer(0));
          output.close();
          expected += 6 + 3 * i;
        }
      }

      try (final WeldValue input = struct(address).toValue();
           final WeldValue output = finalize.run(input)) {
        Assert.assertEquals(expected, output.result(i64).getLong(0));
      }
    }
  }

  @Test
  public void compileAndRunPassBuilder2() {
    String code1 = "|index: i32| let m = merger[i64, +]; merge(m, 7L)";
    String code2 = "|index: i32, m: merger[i64, +]| let mm = merge(m, 10L); {[index, 1], [result(mm), 1L]}";
    try(final WeldModule initialize = WeldModule.compile(code1);
        final WeldModule finalize = WeldModule.compile(code2);
        final WeldStruct arguments1 = struct(2);
        final WeldValue input1 = arguments1.toValue();
        final WeldValue appender = initialize.run(input1)) {
      long address = appender.result(pointer).getPointer(0);
      try (final WeldStruct arguments2 = struct(2, address);
           final WeldValue input2 = arguments2.toValue();
           final WeldValue output = finalize.run(input2)) {
        final WeldStruct struct = output.result(vecOf(i32), vecOf(i64));
        Assert.assertEquals(2, struct.getVec(0).getInt(0));
        Assert.assertEquals(1, struct.getVec(0).getInt(1));
        Assert.assertEquals(17L, struct.getVec(1).getLong(0));
        Assert.assertEquals(1L, struct.getVec(1).getLong(1));
      }
    }
  }
}
