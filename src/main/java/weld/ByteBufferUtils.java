package weld;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
/**
 * Helper class for working with Bytebuffers.
 */
class ByteBufferUtils {
  /**
   * Allocate a direct ByteBuffer that uses the native bit ordering.
   */
  static ByteBuffer allocateDirect(int size) {
    return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
  }

  /**
   * Make sure the ByteBuffer we use is direct.
   */
  static ByteBuffer toDirect(final ByteBuffer input) {
    if (input.isDirect()) {
      return input;
    }
    final ByteBuffer direct = allocateDirect(input.remaining());
    return direct.put(input);
  }
}
