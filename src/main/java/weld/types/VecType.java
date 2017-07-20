package weld.types;

/**
 * Vector type.
 */
public final class VecType implements Type {
  /**
   * Type vecOf the element vecOf the vector.
   */
  private final Type elementType;

  public VecType(final Type elementType) {
    super();
    if (elementType == null) {
      throw new NullPointerException("elementType");
    }
    this.elementType = elementType;
  }

  @Override
  public String name() {
    return "vec[" + elementType.name() + "]";
  }

  @Override
  public int alignment() {
    return 8;
  }

  @Override
  public int size() {
    return 16;
  }

  public Type elementType() {
    return elementType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return elementType.equals(((VecType) o).elementType);
  }

  @Override
  public int hashCode() {
    return elementType != null ? elementType.hashCode() : 0;
  }

  @Override
  public String toString() {
    return name();
  }

  public static VecType vecOf(final Type elementType) {
    return new VecType(elementType);
  }
}
