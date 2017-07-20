package weld.types;

/**
 * Enum describing weld's primitive types.
 */
public enum PrimitiveType implements Type {
  /**
   * Boolean.
   */
  bool(1),
  /**
   * 8-bit signed integer.
   */
  i8(1),
  /**
   * 32-bit signed integer.
   */
  i32(4),
  /**
   * 64-bit signed integer.
   */
  i64(8),
  /**
   * 32-bit floating point.
   */
  f32(4),
  /**
   * 64-bit floating point.
   */
  f64(8),
  /**
   * Opaque pointer. This is used to pass weld structures between plans.
   */
  Pointer(8);

  private final int size;

  PrimitiveType(int size) {
    this.size = size;
  }

  @Override
  public int alignment() {
    return size;
  }

  @Override
  public int size() {
    return size;
  }
}
