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

import eu.europa.ted.eforms.viewer.helpers.SdkConstants.ResourceType;

public class SdkResourcesLoader {
  public static final String DEFAULT_SDK_ROOT = "eforms-sdk";

  private String root = DEFAULT_SDK_ROOT;

  private static final SdkResourcesLoader INSTANCE = new SdkResourcesLoader();

  private SdkResourcesLoader() {
  }

  public SdkResourcesLoader setRoot(Optional<String> root) {
    this.root = root.orElse(DEFAULT_SDK_ROOT);
    return this;
  }

  public Path getResourceAsPath(final ResourceType resourceType, final String sdkVersion) {
    return getResourceAsPath(resourceType, sdkVersion, null);
  }

  public Path getResourceAsPath(final ResourceType resourceType, String sdkVersion, final String filename) {
    Validate.notEmpty(sdkVersion, "Undefined SDK resources version");

    sdkVersion = StringUtils.removeStart(sdkVersion, "eforms-sdk-");

    Path result = Path.of(
        root, Optional.ofNullable(sdkVersion).orElse(StringUtils.EMPTY), Optional.ofNullable(resourceType)
            .map(SdkConstants.ResourceType::getPath).orElse(Path.of(StringUtils.EMPTY)).toString(),
        Optional.ofNullable(filename).orElse(StringUtils.EMPTY)).toAbsolutePath();

    Validate.isTrue(Files.exists(result, new LinkOption[0]),
        MessageFormat.format("Resource [{0}] does not exist", result));

    return result;
  }

  public InputStream getResourceAsStream(final ResourceType resourceType, String sdkVersion, final String filename)
      throws IOException {
    return Files.newInputStream(getResourceAsPath(resourceType, sdkVersion, filename));
  }

  public static SdkResourcesLoader getInstance() {
    return INSTANCE;
  }
}
