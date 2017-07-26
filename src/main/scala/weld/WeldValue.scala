package weld

import java.nio.{ByteBuffer, ByteOrder}

import scala.annotation.varargs

/**
 * This is a wrapper around the weld value object.
 *
 * Note that this object must be closed after usage.
 */
class WeldValue private[weld](handle: Long, val size: Long = -1) extends WeldManaged(handle) {
  override protected def doClose(): Unit = WeldJNI.weld_value_free(handle)

  /**
   * Get the address to the data this value encapsulates.
   */
  def getPointer: Long = {
    checkAccess()
    WeldJNI.weld_value_pointer(handle)
  }

  /**
   * Get the weld run ID this value is associated with.
   */
  def getRunId: Long = {
    checkAccess()
    WeldJNI.weld_value_run(handle)
  }

  /**
   * Get the result of a weld module run.
   */
  def result(structType: StructType): WeldStruct = {
    checkAccess()
    new WeldStruct(getPointer, structType)
  }

  /**
   * Get the result of a weld module run.
   */
  @varargs
  def result(fieldTypes: WeldType*): WeldStruct = result(StructType(fieldTypes))
}

object WeldValue {
  private def directByteBuffer(size: Int) = {
    ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder)
  }

  /**
   * Create an empty [[WeldValue]]
   */
  def empty(): WeldValue = apply(directByteBuffer(0))

  /**
   * Create a [[WeldValue]] using a (direct) [[ByteBuffer]].
   */
  def apply(buffer: ByteBuffer): WeldValue = {
    val direct = if (!buffer.isDirect) {
      val replacement = directByteBuffer(buffer.limit())
      replacement.put(buffer)
    } else {
      buffer
    }
    apply(WeldJNI.weld_get_buffer_pointer(direct), buffer.limit())
  }

  /**
   * Create a [[WeldValue]] from the given pointer.
   */
  def apply(pointer: Long): WeldValue = apply(pointer, -1L)

  /**
   * Create a sized [[WeldValue]] from the given pointer.
   */
  def apply(pointer: Long, size: Long): WeldValue = {
    new WeldValue(WeldJNI.weld_value_new(pointer), size)
  }
}
