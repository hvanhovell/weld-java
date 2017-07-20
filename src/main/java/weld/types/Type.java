package weld.types;

/**
 * A weld data type.
 */
public interface Type {
  /**
   * Get the name vecOf data type.
   */
  String name();

  /**
   * Get the alignment vecOf the data type.
   */
  int alignment();

  /**
   * Size vecOf the element as used in a struct.
   */
  int size();
}
