package weld;

public class WeldModule implements AutoCloseable {
  private final long handle;

  private WeldModule(final long handle) {
    super();
    this.handle = handle;
  }

  public void close() {
    WeldJNI.weld_module_free(this.handle);
  }

  public WeldValue run(final WeldValue input) {
    try(final WeldConf conf = new WeldConf()) {
      return run(conf, input);
    }
  }

  public WeldValue run(final WeldConf conf, final WeldValue input) {
    final WeldError error = new WeldError();
    final WeldValue output = new WeldValue(
      WeldJNI.weld_module_run(
        this.handle,
        conf.handle,
        input.handle,
        error.handle),
        false);
    if (error.getCode() != 0) {
      final WeldException e = new WeldException(error);
      error.close();
      output.close();
      throw e;
    }
    return output;
  }

  public static WeldModule compile(final String code) {
    try(final WeldConf conf = new WeldConf()) {
      return compile(conf, code);
    }
  }

  public static WeldModule compile(final WeldConf conf, final String code) {
    final WeldError error = new WeldError();
    final WeldModule module = new WeldModule(
      WeldJNI.weld_module_compile(
        code,
        conf.handle,
        error.handle));
    if (error.getCode() != 0) {
      final WeldException e = new WeldException(error);
      error.close();
      module.close();
      throw e;
    }
    return module;
  }
}
