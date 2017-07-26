package weld

import sun.misc.{Cleaner, Unsafe}

/**
 * Platform dependent operations.
 */
object Platform {
  private val UNSAFE = {
    val ctor = classOf[Unsafe].getDeclaredConstructor()
    ctor.setAccessible(true)
    ctor.newInstance()
  }

  /**
   * Register an AutoCloseable for automatic clean-up as soon as it gets garbage collected.
   */
  def registerForCleanUp(closeable: AutoCloseable): Unit = {
    Cleaner.create(closeable, new Runnable {
      override def run(): Unit = {
        try closeable.close() catch {
          case e: Exception =>
            throw new RuntimeException(e)
        }
      }
    })
  }

  def allocateMemory(size: Long): Long = UNSAFE.allocateMemory(size)

  def freeMemory(address: Long): Unit = {
    UNSAFE.freeMemory(address)
  }

  def copyMemory(src: Long, dest: Long, length: Long): Unit = {
    UNSAFE.copyMemory(null, src, null, dest, length)
  }

  def getByte(address: Long): Byte = UNSAFE.getByte(address)

  def putByte(address: Long, value: Byte): Unit = {
    UNSAFE.putByte(address, value)
  }

  def putBytes(address: Long, values: Array[Byte]): Unit = {
    UNSAFE.copyMemory(values, Unsafe.ARRAY_BOOLEAN_BASE_OFFSET, null, address, values.length)
  }

  def getInt(address: Long): Int = UNSAFE.getInt(address)

  def putInt(address: Long, value: Int): Unit = {
    UNSAFE.putInt(address, value)
  }

  def putInts(address: Long, values: Array[Int]): Unit = {
    UNSAFE.copyMemory(values, Unsafe.ARRAY_INT_BASE_OFFSET, null, address, values.length * 4)
  }

  def getFloat(address: Long): Float = UNSAFE.getFloat(address)

  def putFloat(address: Long, value: Float): Unit = {
    UNSAFE.putFloat(address, value)
  }

  def putFloats(address: Long, values: Array[Float]): Unit = {
    UNSAFE.copyMemory(values, Unsafe.ARRAY_FLOAT_BASE_OFFSET, null, address, values.length * 4)
  }

  def getLong(address: Long): Long = UNSAFE.getLong(address)

  def putLong(address: Long, value: Long): Unit = {
    UNSAFE.putLong(address, value)
  }

  def putLongs(address: Long, values: Array[Long]): Unit = {
    UNSAFE.copyMemory(values, Unsafe.ARRAY_LONG_BASE_OFFSET, null, address, values.length * 8)
  }

  def getDouble(address: Long): Double = UNSAFE.getDouble(address)

  def putDouble(address: Long, value: Double): Unit = {
    UNSAFE.putDouble(address, value)
  }

  def putDoubles(address: Long, values: Array[Double]): Unit = {
    UNSAFE.copyMemory(values, Unsafe.ARRAY_DOUBLE_BASE_OFFSET, null, address, values.length * 8)
  }
}
