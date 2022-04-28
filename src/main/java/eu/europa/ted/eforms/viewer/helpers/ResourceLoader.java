package eu.europa.ted.eforms.viewer.helpers;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.lang3.StringUtils;

public class ResourceLoader {

  private ResourceLoader() {
    throw new AssertionError("Utility class.");
  }

  /**
   * @param resourcePath Path relative to "src/main/resources/" if you are in a Maven project.
   *
   * @return The path of the specified resource
   */
  public static Path getResourceAsPath(final String resourcePath) {
    assert StringUtils.isNotBlank(resourcePath) : "resourcePath is blank";
    assert !resourcePath.startsWith("/") : String
        .format("resourcePath should not start with forward slash: %s", resourcePath);
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try {
      return Paths.get(classLoader.getResource(resourcePath).toURI());
    } catch (final URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param resourcePath Path relative to "src/main/resources/" if you are in a Maven project.
   *
   * @return The inputstream to the specified resource
   */
  public static InputStream getResourceAsStream(final String resourcePath) {
    assert StringUtils.isNotBlank(resourcePath) : "resourcePath is blank";
    assert !resourcePath.startsWith("/") : String
        .format("resourcePath should not start with forward slash: %s", resourcePath);
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    return classLoader.getResourceAsStream(resourcePath);
  }

  /**
   * @param resourcePath Path relative to "src/main/resources/" if you are in a Maven project.
   *
   * @return The inputstream to the specified resource
   */
  public static InputStream getResourceAsStream(final ClassLoader classLoader,
      final String resourcePath) {
    assert StringUtils.isNotBlank(resourcePath) : "resourcePath is blank";
    assert !resourcePath.startsWith("/") : String
        .format("resourcePath should not start with forward slash: %s", resourcePath);
    return classLoader.getResourceAsStream(resourcePath);
  }

}
