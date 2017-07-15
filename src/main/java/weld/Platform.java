package weld;

import sun.misc.Cleaner;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;

/**
 * Platform dependent operations.
 */
class Platform {
  private static final Unsafe UNSAFE;

  static {
    Unsafe unsafe;
    try {
      Constructor<Unsafe> ctor = Unsafe.class.getDeclaredConstructor();
      ctor.setAccessible(true);
      unsafe = ctor.newInstance();
    } catch (ReflectiveOperationException e) {
      unsafe = null;
    }
    UNSAFE = unsafe;
  }

  /**
   * Register an AutoCloseable for automatic clean-up as soon as it gets garbage collected.
   */
  public static void registerForCleanUp(AutoCloseable closeable) {
    Cleaner.create(closeable, () -> {
      try {
        closeable.close();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  public static long allocateMemory(long size) {
    return UNSAFE.allocateMemory(size);
  }

  public static void freeMemory(long address) {
    UNSAFE.freeMemory(address);
  }

  public static void copyMemory(long src, long dest, long length) {
    UNSAFE.copyMemory(null, src, null, dest, length);
  }

  public static byte getByte(long address) {
    return UNSAFE.getByte(address);
  }

  public static void putByte(long address, byte value) {
    UNSAFE.putByte(address, value);
  }

  public static void putBytes(long address, byte[] values) {
    UNSAFE.copyMemory(values, Unsafe.ARRAY_BOOLEAN_BASE_OFFSET, null, address, values.length);
  }

  public static int getInt(long address) {
    return UNSAFE.getInt(address);
  }

  public static void putInt(long address, int value) {
    UNSAFE.putInt(address, value);
  }

  public static void putInts(long address, int[] values) {
    UNSAFE.copyMemory(values, Unsafe.ARRAY_INT_BASE_OFFSET, null, address, values.length * 4);
  }

  public static float getFloat(long address) {
    return UNSAFE.getFloat(address);
  }

  public static void putFloat(long address, float value) {
    UNSAFE.putFloat(address, value);
  }

  public static void putFloats(long address, float[] values) {
    UNSAFE.copyMemory(values, Unsafe.ARRAY_FLOAT_BASE_OFFSET, null, address, values.length * 4);
  }

  public static long getLong(long address) {
    return UNSAFE.getLong(address);
  }

  public static void putLong(long address, long value) {
    UNSAFE.putLong(address, value);
  }

  public static void putLongs(long address, long[] values) {
    UNSAFE.copyMemory(values, Unsafe.ARRAY_LONG_BASE_OFFSET, null, address, values.length * 8);
  }

  public static double getDouble(long address) {
    return UNSAFE.getDouble(address);
  }

  public static void putDouble(long address, double value) {
    UNSAFE.putDouble(address, value);
  }

  public static void putDoubles(long address, double[] values) {
    UNSAFE.copyMemory(values, Unsafe.ARRAY_DOUBLE_BASE_OFFSET, null, address, values.length * 8);
  }
}
