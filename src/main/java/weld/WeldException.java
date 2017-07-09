package weld;

public class WeldException extends RuntimeException {
  private final int code;

  public WeldException(final int code, final String message) {
    super(message);
    this.code = code;
  }

  public WeldException(final String message) {
    this(-1, message);
  }

  public WeldException(final WeldError error) {
    this(error.getCode(), error.getMessage());
  }

  public int getCode() {
    return this.code;
  }
}
