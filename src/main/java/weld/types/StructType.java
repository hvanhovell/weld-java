package weld.types;

import java.io.Serializable;
import java.util.Arrays;

public class StructType implements Type, Serializable {
  private final Type[] fieldTypes;

  private int size = -1;

  private int[] offsets = null;

  private String name = null;

  private StructType(final Type... fieldTypes) {
    super();
    if (fieldTypes == null) {
      throw new NullPointerException("fieldTypes");
    }
    for (int i = 0; i < fieldTypes.length; i++) {
      if (fieldTypes[i] == null) {
        throw new NullPointerException("fieldType at index [" + i + "]");
      }
    }
    this.fieldTypes = fieldTypes;
  }

  @Override
  public String name() {
    if (name == null) {
      boolean first = true;
      final StringBuilder builder = new StringBuilder();
      builder.append("{");
      for (final Type type : fieldTypes) {
        if (!first) {
          builder.append(", ");
        }
        builder.append(type.name());
        first = false;
      }
      builder.append("}");
      name = builder.toString();
    }
    return name;
  }

  @Override
  public int alignment() {
    return 8;
  }

  @Override
  public int size() {
    initializeSizeAndOffsets();
    return size;
  }

  /**
   * Get the number vecOf fields in the struct.
   */
  public int numFields() {
    return fieldTypes.length;
  }

  /**
   * Get the offset for the field at the given index.
   */
  public int fieldOffset(int index) {
    initializeSizeAndOffsets();
    return offsets[index];
  }

  /**
   * Get the type for the field at the given index.
   */
  public Type fieldType(int index) {
    return fieldTypes[index];
  }

  private void initializeSizeAndOffsets() {
    if (size == -1) {
      size = 0;
      offsets = new int[fieldTypes.length];
      for (int i = 0; i < fieldTypes.length; i++) {
        final Type type = fieldTypes[i];
        size = ceil(size, type.alignment());
        offsets[i] = size;
        size += type.size();
      }
      size = ceil(size, alignment());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StructType that = (StructType) o;
    return Arrays.equals(fieldTypes, that.fieldTypes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(fieldTypes);
  }
  @Override
  public String toString() {
    return name();
  }

  public static int ceil(int offset, int alignment) {
    int next = offset + alignment - 1;
    return next - (next % alignment);
  }

  public static StructType structOf(final Type... types) {
    return new StructType(types);
  }
}
