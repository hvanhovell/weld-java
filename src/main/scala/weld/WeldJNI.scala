package weld

import java.nio.ByteBuffer
import java.nio.file.{Files, Path, Paths}

import scala.util.control.NonFatal

object WeldJNI {
  /**
   * Determine the path and the extensions of the native libraries in the weld-java jar.
   */
  private def nativeResourcePathAndExtentions: Option[String] = {
    val arch = System.getProperty("os.arch").toLowerCase
    val platform = System.getProperty("os.name").toLowerCase

    // We currently only support 64-bit intel platforms on Mac/Linux.
    val is64bitIntel = (arch.contains("86") || arch.contains("amd")) && arch.contains("64")
    if (is64bitIntel && (platform.contains("nux") || platform.contains("nix"))) {
      Some("META-INF/lib/linux_64")
    } else if (is64bitIntel && platform.contains("mac")) {
      Some("META-INF/lib/osx_64")
    } else {
      None
    }
  }

  /**
   * Load the native library both in java and in weld.
   */
  private def loadNativeLibrary(lib: String): Unit = {
    System.load(lib)
    // We need to explicitly load the weld_java library for linux.
    Weld.loadLibrary(lib)
  }

  /**
   * Load the native libraries from the classpath.
   */
  private def loadNativeLibrariesFromClassPath(): Unit = {
    nativeResourcePathAndExtentions.foreach { path =>
      // Create a temporary directory to place the native libraries.
      val target = Files.createTempDirectory("weld-java")
      target.toFile.deleteOnExit()

      // Copy the native library from the jar file into the temporary directory.
      def copy(name: String): Path = {
        val lib = System.mapLibraryName(name)
        val libraryPath = s"$path/$lib"
        val input = getClass.getClassLoader.getResourceAsStream(libraryPath)
        if (input == null) {
          throw new NullPointerException(s"Cannot find resource on classpath: $libraryPath")
        }
        val output = target.resolve(lib)
        try {
          Files.copy(input, output)
          output
        } catch {
          case NonFatal(e) =>
            input.close()
            throw e
        }
      }

      // Load the libweld-java library
      loadNativeLibrary(copy("weld_java").toString)
    }
  }

  /**
   * Load the native libraries (weld_java & weldrt). This method first tries to load the
   * libraries from the directory defined in the 'weld.library.path' environment
   * variable. If this fails, it falls back to unpacking the libraries from the jar into
   * a temporary location, and then loading them from that temporary location.
   */
  private def loadNativeLibraries(): Unit = {
    // Try to load the library from the library path. This is easier for testing/development.
    val weldLibraryPath = System.getProperty("weld.library.path")
    if (weldLibraryPath != null) {
      try {
        val path = Paths.get(weldLibraryPath).toAbsolutePath
        loadNativeLibrary(path.resolve(System.mapLibraryName("weld_java")).toString)
      } catch {
        case _: SecurityException | _: UnsatisfiedLinkError =>
          loadNativeLibrariesFromClassPath()
      }
    } else {
      loadNativeLibrariesFromClassPath()
    }
  }
  loadNativeLibraries()

  @native
  private[weld] def weld_value_new(pointer: Long): Long

  @native
  private[weld] def weld_value_pointer(handle: Long): Long

  @native
  private[weld] def weld_get_buffer_pointer(buffer: ByteBuffer): Long

  @native
  private[weld] def weld_value_run(handle: Long): Long

  @native
  private[weld] def weld_value_memory_usage(handle: Long): Long

  @native
  private[weld] def weld_value_free(handle: Long): Unit

  @native
  private[weld] def weld_module_compile(code: String, conf: Long, error: Long): Long

  @native
  private[weld] def weld_module_run(module: Long, conf: Long, input: Long, error: Long): Long

  @native
  private[weld] def weld_module_free(module: Long): Unit

  @native
  private[weld] def weld_error_new(): Long

  @native
  private[weld] def weld_error_free(handle: Long): Unit

  @native
  private[weld] def weld_error_code(handle: Long): Int

  @native
  private[weld] def weld_error_message(handle: Long): String

  @native
  private[weld] def weld_conf_new(): Long

  @native
  private[weld] def weld_conf_free(handle: Long): Unit

  @native
  private[weld] def weld_conf_get(handle: Long, key: String): String

  @native
  private[weld] def weld_conf_set(handle: Long, key: String, value: String): Unit

  @native
  private[weld] def weld_load_library(filename: String, error: Long): Unit

  @native
  private[weld] def weld_set_log_level(loglevel: String): Unit
}
