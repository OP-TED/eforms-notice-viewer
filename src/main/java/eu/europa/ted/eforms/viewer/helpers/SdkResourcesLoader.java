package eu.europa.ted.eforms.viewer.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ted.eforms.viewer.helpers.SdkConstants.ResourceType;

public class SdkResourcesLoader {
  private static final Logger logger = LoggerFactory.getLogger(SdkResourcesLoader.class);

  public static final String DEFAULT_SDK_ROOT = "eforms-sdk";

  private String version;
  private String root = DEFAULT_SDK_ROOT;

  private static final SdkResourcesLoader INSTANCE = new SdkResourcesLoader();

  private SdkResourcesLoader() {
  }

  public SdkResourcesLoader setVersion(String version) {
    this.version = version;
    return this;
  }

  public SdkResourcesLoader setRoot(Optional<String> root) {
    this.root = root.orElse(DEFAULT_SDK_ROOT);
    return this;
  }

  public Path getResourceAsPath(final String filename) {
    return getResourceAsPath(null, filename);
  }

  public Path getResourceAsPath(final ResourceType resourceType) {
    return getResourceAsPath(resourceType, null);
  }

  public Path getResourceAsPath(final ResourceType resourceType, final String filename) {
    Validate.notEmpty(version, "Undefined SDK resources version");

    Path result = Path
        .of(root, version, Optional.ofNullable(resourceType).map(SdkConstants.ResourceType::getPath)
            .orElse(Path.of(StringUtils.EMPTY)).toString(), Optional.ofNullable(filename).orElse(StringUtils.EMPTY))
        .toAbsolutePath();

    Validate.isTrue(Files.exists(result, new LinkOption[0]),
        MessageFormat.format("Resource [{0}] does not exist", result));

    return result;
  }

  public InputStream getResourceAsStream(final String filename) throws IOException {
    return getResourceAsStream(null, filename);
  }

  public InputStream getResourceAsStream(final ResourceType resourceType) throws IOException {
    return getResourceAsStream(resourceType, null);
  }

  public InputStream getResourceAsStream(final ResourceType resourceType, final String filename) throws IOException {
    return Files.newInputStream(getResourceAsPath(resourceType, filename));
  }

  public static SdkResourcesLoader getInstance() {
    return INSTANCE;
  }
}
