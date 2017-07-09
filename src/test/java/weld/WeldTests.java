package weld;

import java.nio.ByteBuffer;

import org.junit.*;
import org.junit.Assert;

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
    final ByteBuffer buffer = ByteBuffer.allocateDirect(8);
    buffer.putInt(23);
    buffer.putInt(-1);
    buffer.flip();
    try(final WeldValue value = new WeldValue(buffer)) {
      Assert.assertTrue(value.getPointer() > 0);
    }
  }

  /**
   * Test compilation failure.
   */
  @Test
  public void compileTestFail() {
    String code = "foo";
    try {
      final WeldModule module = WeldModule.compile(code);
      Assert.fail("Compilation should have failed.");
    } catch(final WeldException e) {
      Assert.assertEquals(e.getCode(), 1);
      Assert.assertEquals(e.getMessage(), "Undefined symbol foo in uniquify");
    }
  }

  @Test
  public void compileAndRunSimpleTest() {
    String code = "|| i32(0.251 * 4.0)";
    try(final WeldModule module = WeldModule.compile(code)) {
      // TODO add a way to decode a result properly.
      module.run(new WeldValue());
    }
  }

  @Test
  public void compileAndRunWithSingleArgumentTest() {
    String code = "|x:i32| {x + 1, x - 1}";
    try(final WeldModule module = WeldModule.compile(code)) {
      final ByteBuffer buffer = ByteBuffer.allocateDirect(4);
      buffer.putInt(42);
      buffer.flip();
      module.run(new WeldValue(buffer));
    }
  }
}
