package eu.europa.ted.eforms.sdk.selector.resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.SdkConstants.SdkResource;
import eu.europa.ted.eforms.viewer.helpers.SdkResourceLoader;
import eu.europa.ted.maven.MavenBooter;

public class SdkDownloader {
  private static final Logger logger = LoggerFactory.getLogger(SdkDownloader.class);

  private SdkDownloader() {
  }

  public static void downloadSdk(String sdkVersion) throws IOException {
    downloadSdk(sdkVersion, null);
  }

  public static void downloadSdk(String sdkVersion, String rootDir) throws IOException {
    Path sdkDir = Path.of(Optional.ofNullable(rootDir).orElse(SdkConstants.DEFAULT_SDK_ROOT), sdkVersion);

    try {
      if (sdkExistsAt(sdkVersion, sdkDir)) {
        logger.debug("SDK [{}] found at [{}]. No download required.", sdkVersion, sdkDir);
      } else {
        logger.info("Downloading eForms SDK [{}]", sdkVersion);
        logger.debug("Target directory: {}", sdkDir.toAbsolutePath());

        String artifactVersion = getLatestSdkVersion(sdkVersion);
        SdkUnpacker.unpack(resolve(artifactVersion), sdkDir);

        logger.debug("Successfully downloaded eForms SDK [{}] onto [{}].", sdkVersion, sdkDir.toAbsolutePath());
      }
    } catch (IOException e) {
      logger.debug("Failed to download eForms SDK {}: {}", sdkVersion, e.getMessage());
      throw e;
    } catch (VersionRangeResolutionException | ArtifactResolutionException e) {
      logger.debug("Failed to download eForms SDK {}: {}", sdkVersion, e.getMessage());
      throw new IOException(e);
    }
  }

  private static File resolve(String artifactVersion) throws ArtifactResolutionException {
    Validate.notBlank(artifactVersion, "Undefined SDK version.");

    logger.debug("Resolving eforms-sdk artifact with version [{}]", artifactVersion);

    Artifact artifact = new DefaultArtifact(SdkConstants.SDK_GROUP_ID, SdkConstants.SDK_ARTIFACT_ID, "jar",
        artifactVersion);

    logger.debug("Maven coordinates for eforms-sdk artifact: {}", artifact);

    File artifactFile = MavenBooter.resolveArtifact(artifact);

    logger.debug("Resolved [{}] as [{}].", artifact, artifactFile);

    return artifactFile;
  }

  private static boolean sdkExistsAt(String sdkVersion, Path sdkDir) {
    if (StringUtils.isBlank(sdkVersion) || sdkDir == null) {
      return false;
    }

    Path fieldsJsonPath = null;

    try {
      fieldsJsonPath = SdkResourceLoader.INSTANCE.getResourceAsPath(SdkResource.FIELDS_JSON, sdkVersion);
    } catch (Exception e) {
      return false;
    }

    try (JsonParser jsonParser = new JsonFactory().createParser(fieldsJsonPath.toFile())) {
      while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
        if ("sdkVersion".equals(jsonParser.getCurrentName())) {
          jsonParser.nextToken();
          String sdkVersionOnFieldsJson = jsonParser.getValueAsString();
          return sdkVersion.equals(
              new SdkVersion(StringUtils.removeStart(sdkVersionOnFieldsJson, "eforms-sdk-")).toStringWithoutPatch());
        }
      }
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  /**
   * Discovers the latest available minor version for a given base version in the
   * format <major.minor>. If the major of the base version is zero, then the
   * second digit is regarded as major. E.g.: - For base version 0.6, the result
   * is the largest found number between 0.6 and 0.7 - For base version 1.0, the
   * result is the largest found number between 1.0 and 1.1
   * 
   * @param sdkVersion The base version
   * @return
   */
  private static String getLatestSdkVersion(final String baseVersion) throws VersionRangeResolutionException {
    SdkVersion currentVersion = new SdkVersion(baseVersion);

    Artifact artifact = new DefaultArtifact(SdkConstants.SDK_GROUP_ID, SdkConstants.SDK_ARTIFACT_ID, "jar",
        MessageFormat.format("[{0},{1})", currentVersion.getMajor(), currentVersion.getNextMajor()));

    VersionRangeResult versions = MavenBooter.resolveVersionRange(artifact);
    try {
      if (currentVersion.getMajor().equals("0")) {
        return versions.getVersions().stream().map((Version version) -> new SdkVersion(version.toString()))
            .filter((SdkVersion v) -> v.getMajor().equals(currentVersion.getMajor())
                && v.getMinor().equals(currentVersion.getMinor()))
            .max((SdkVersion i, SdkVersion j) -> i.compareTo(j)).orElseThrow().toString();
      } else {
        Version highestVersion = versions.getHighestVersion();

        if (highestVersion == null) {
          throw new NoSuchElementException();
        }

        return highestVersion.toString();
      }
    } catch (NoSuchElementException e) {
      String snapshotVersion = MessageFormat.format("{0}.0-SNAPSHOT", baseVersion);
      logger.warn("No artifacts were found for SDK version [{}]. Trying with [{}]", baseVersion, snapshotVersion);

      artifact = new DefaultArtifact(MessageFormat.format("{0}:{1}:{2}", SdkConstants.SDK_GROUP_ID,
          SdkConstants.SDK_ARTIFACT_ID, snapshotVersion));

      versions = MavenBooter.resolveVersionRange(artifact);

      if (CollectionUtils.isEmpty(versions.getVersions())) {
        throw new IllegalArgumentException(
            MessageFormat.format("No artifacts were found for SDK version [{0}]", snapshotVersion));
      }

      return versions.getVersions().get(0).toString();
    }
  }
}
