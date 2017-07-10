package weld;

import org.junit.*;
import org.junit.Assert;

import static weld.WeldStruct.struct;

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
      Assert.assertEquals(conf.get("x.y"), "test");
    }
  }

  /**
   * Test the error class.
   */
  @Test
  public void errorTest() {
    try(final WeldError error = new WeldError()) {
      Assert.assertEquals(error.getCode(), 0);
      Assert.assertEquals(error.getMessage(), "Success");
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
      Assert.assertEquals(struct.getInt(0), 23);
      Assert.assertEquals(struct.getInt(4), -1);
    }
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
  public void compileAndRunSimpleTest() {
    String code = "|| i32(0.251 * 4.0)";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue output = module.run(new WeldValue())) {
      final WeldStruct struct = output.result(4);
      Assert.assertEquals(struct.getInt(0), 1);
    }
  }

  @Test
  public void compileAndRunWithSingleArgumentTest() {
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
  public void compileAndRunWithTwoArgumentTest() {
    String code = "|x:i32, y:i32| [x, y]";
    try(final WeldModule module = WeldModule.compile(code);
        final WeldValue value = struct(42, 76).toValue();
        final WeldValue output = module.run(value)) {
      final WeldVec vec = output.result(16).getVec(0,4);
      Assert.assertEquals(vec.getInt(0), 42);
      Assert.assertEquals(vec.getInt(4), 76);
    }
  }
}
