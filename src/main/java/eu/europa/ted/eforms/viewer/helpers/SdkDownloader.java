package eu.europa.ted.eforms.viewer.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenWorkingSession;
import org.jboss.shrinkwrap.resolver.api.maven.PackagingType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenWorkingSessionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdkDownloader {
  private static final Logger log = LoggerFactory.getLogger(SdkDownloader.class);

  private SdkDownloader() {}

  public static void downloadSdk(String sdkVersion) throws IOException {
    downloadSdk(sdkVersion, null);
  }

  public static void downloadSdk(String sdkVersion, String rootDir) throws IOException {
    Path sdkDir =
        Path.of(Optional.ofNullable(rootDir).orElse(SdkConstants.DEFAULT_SDK_ROOT), sdkVersion);

    String artifactVersion =
        Optional.ofNullable(SdkConstants.SDK_VERSIONS_MAP.get(sdkVersion)).orElse(sdkVersion);

    if (sdkExistsAt(artifactVersion, sdkDir)) {
      log.debug("SDK [{}] found at [{}]. No download required.", sdkVersion, sdkDir);
    } else {
      log.info("Downloading eForms SDK [{}]", sdkVersion);
      log.debug("Target directory: {}", sdkDir.toAbsolutePath());

      SdkUnpacker.unpack(resolve(artifactVersion), sdkDir);

      log.debug("Successfully downloaded eForms SDK [{}] onto [{}].", sdkVersion,
          sdkDir.toAbsolutePath());
    }
  }

  private static File resolve(String artifactVersion) {
    Validate.notBlank(artifactVersion, "Undefined SDK version.");

    log.debug("Resolving eforms-sdk artifact with version [{}]", artifactVersion);

    MavenCoordinate coords =
        MavenCoordinates.createCoordinate(SdkConstants.SDK_GROUP_ID, SdkConstants.SDK_ARTIFACT_ID,
            artifactVersion, PackagingType.of(SdkConstants.SDK_PACKAGING), StringUtils.EMPTY);
    log.debug("Maven coordinates for eforms-sdk artifact: {}", coords.toCanonicalForm());

    File artifactFile =
        Maven.resolver().resolve(coords.toCanonicalForm()).withoutTransitivity().asSingleFile();
    log.debug("Resolved [{}] as [{}].", coords.toCanonicalForm(), artifactFile);

    return artifactFile;
  }

  private static boolean sdkExistsAt(String sdkVersion, Path sdkDir) {
    if (StringUtils.isBlank(sdkVersion) || sdkDir == null) {
      return false;
    }

    File pomFile = Path.of(sdkDir.toString(), "pom.xml").toFile();
    if (!Files.isRegularFile(Path.of(sdkDir.toString(), "pom.xml"))) {
      return false;
    }

    MavenWorkingSession mws =
        ((MavenWorkingSessionContainer) Maven.resolver()).getMavenWorkingSession();
    mws.loadPomFromFile(pomFile);

    return sdkVersion.equals(mws.getParsedPomFile().getVersion());
  }
}
