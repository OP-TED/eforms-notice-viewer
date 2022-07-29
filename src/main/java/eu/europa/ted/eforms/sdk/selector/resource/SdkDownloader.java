package eu.europa.ted.eforms.sdk.selector.resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenVersionRangeResult;
import org.jboss.shrinkwrap.resolver.api.maven.MavenWorkingSession;
import org.jboss.shrinkwrap.resolver.api.maven.PackagingType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates;
import org.jboss.shrinkwrap.resolver.impl.maven.MavenWorkingSessionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.sdk.SdkConstants;

public class SdkDownloader {
  private static final Logger logger = LoggerFactory.getLogger(SdkDownloader.class);

  static {
    String mavenOpts = System.getenv("MAVEN_OPTS");
    if (StringUtils.isNotBlank(mavenOpts)) {
      System.setProperty("MAVEN_OPTS", System.getenv("MAVEN_OPTS"));
    }
  }

  private SdkDownloader() {}

  public static void downloadSdk(String sdkVersion) throws IOException {
    downloadSdk(sdkVersion, null);
  }

  public static void downloadSdk(String sdkVersion, String rootDir) throws IOException {
    Path sdkDir =
        Path.of(Optional.ofNullable(rootDir).orElse(SdkConstants.DEFAULT_SDK_ROOT), sdkVersion);

    String artifactVersion = getLatestSdkVersion(sdkVersion);

    if (sdkExistsAt(artifactVersion, sdkDir)) {
      logger.debug("SDK [{}] found at [{}]. No download required.", sdkVersion, sdkDir);
    } else {
      logger.info("Downloading eForms SDK [{}]", sdkVersion);
      logger.debug("Target directory: {}", sdkDir.toAbsolutePath());

      SdkUnpacker.unpack(resolve(artifactVersion), sdkDir);
      resolve(artifactVersion);

      logger.debug("Successfully downloaded eForms SDK [{}] onto [{}].", sdkVersion,
          sdkDir.toAbsolutePath());
    }
  }

  private static ConfigurableMavenResolverSystem getMavenResolver() {
    ConfigurableMavenResolverSystem maven = Maven.configureResolver();

    String userHome = Arrays
        .asList(Optional.ofNullable(System.getProperty("MAVEN_OPTS")).orElse(StringUtils.EMPTY)
            .split("\\s"))
        .stream().filter((String s) -> s.startsWith("-Duser.home="))
        .map((String s) -> s.replaceAll("-Duser.home=", StringUtils.EMPTY)).findFirst()
        .orElse(StringUtils.EMPTY);

    if (StringUtils.isNotBlank(userHome)) {
      Path settingsFile = Path.of(userHome, ".m2", "settings.xml");
      if (settingsFile.toFile().exists()) {
        logger.debug("Using Maven settings file [{}]", settingsFile);
        maven.fromFile(settingsFile.toFile());
      }
    }

    return maven;
  }

  private static File resolve(String artifactVersion) {
    Validate.notBlank(artifactVersion, "Undefined SDK version.");

    logger.debug("Resolving eforms-sdk artifact with version [{}]", artifactVersion);

    MavenCoordinate coords =
        MavenCoordinates.createCoordinate(SdkConstants.SDK_GROUP_ID, SdkConstants.SDK_ARTIFACT_ID,
            artifactVersion, PackagingType.of(SdkConstants.SDK_PACKAGING), StringUtils.EMPTY);
    logger.debug("Maven coordinates for eforms-sdk artifact: {}", coords.toCanonicalForm());

    File artifactFile =
        getMavenResolver().resolve(coords.toCanonicalForm()).withoutTransitivity().asSingleFile();
    logger.info("Resolved [{}] as [{}].", coords.toCanonicalForm(), artifactFile);

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
        ((MavenWorkingSessionContainer) getMavenResolver()).getMavenWorkingSession();
    mws.loadPomFromFile(pomFile);

    return sdkVersion.equals(mws.getParsedPomFile().getVersion());
  }

  /**
   * Discovers the latest available minor version for a given base version in the format
   * <major.minor>. If the major of the base version is zero, then the second digit is regarded as
   * major. E.g.: - For base version 0.6, the result is the largest found number between 0.6 and 0.7
   * - For base version 1.0, the result is the largest found number between 1.0 and 1.1
   * 
   * @param sdkVersion The base version
   * @return
   */
  private static String getLatestSdkVersion(final String baseVersion) {
    SdkVersion currentVersion = new SdkVersion(baseVersion);

    MavenVersionRangeResult versions = getMavenResolver()
        .resolveVersionRange(MessageFormat.format("{0}:{1}:(,{2})", SdkConstants.SDK_GROUP_ID,
            SdkConstants.SDK_ARTIFACT_ID, currentVersion.getNextMajor()));

    try {
      if (currentVersion.getMajor().equals("0")) {
        return versions.getVersions().stream()
            .map((MavenCoordinate coord) -> new SdkVersion(coord.getVersion()))
            .filter((SdkVersion v) -> v.getMinor().equals(currentVersion.getMinor()))
            .max((SdkVersion i, SdkVersion j) -> i.compareTo(j)).orElseThrow().toString();
      } else {
        MavenCoordinate latestCoord = versions.getHighestVersion();

        if (latestCoord == null) {
          throw new NoSuchElementException();
        }

        return latestCoord.getVersion();
      }
    } catch (NoSuchElementException e) {
      String snapshotVersion = MessageFormat.format("{0}.0-SNAPSHOT", baseVersion);
      logger.warn("No artifacts were found for SDK version [{}]. Trying with [{}]", baseVersion,
          snapshotVersion);

      List<MavenCoordinate> snapshotCoords = getMavenResolver()
          .resolveVersionRange(MessageFormat.format("{0}:{1}:{2}", SdkConstants.SDK_GROUP_ID,
              SdkConstants.SDK_ARTIFACT_ID, snapshotVersion))
          .getVersions();

      if (CollectionUtils.isEmpty(snapshotCoords)) {
        throw new IllegalArgumentException(
            MessageFormat.format("No artifacts were found for SDK version [{0}]", snapshotVersion));
      }

      return snapshotCoords.get(0).getVersion();
    }
  }
}
