package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.viewer.helpers.SdkConstants;
import eu.europa.ted.eforms.viewer.helpers.SdkResourcesLoader;

public class NoticeViewerTest {
  private static final Logger logger = LoggerFactory.getLogger(NoticeViewerTest.class);

  private static final String SDK_VERSION = "eforms-sdk-0.6";
  private static final Optional<String> SDK_RESOURCES_ROOT =
      Optional.of(Path.of("target", SdkConstants.DEFAULT_SDK_ROOT).toString());

  @Test
  public void testEfxToHtmlX01English() {
    final String language = "en"; // In english.
    final String noticeXmlFilename = "X01_EEIG"; // "X01_EEIG.xml"
    testGenerateHtmlFromFile(language, noticeXmlFilename);
  }

  @Test
  public void testEfxToHtmlX01Greek() {
    final String language = "el"; // In greek.
    final String noticeXmlFilename = "X01_EEIG"; // "X01_EEIG.xml"
    testGenerateHtmlFromFile(language, noticeXmlFilename);
  }

  @Test
  public void testEfxToHtmlX02() {
    final String language = "en"; // In english.
    final String noticeXmlFilename = "X02_registration";
    testGenerateHtmlFromFile(language, noticeXmlFilename);
  }

  @Test
  public void testEfxToHtml1Minimal() {
    final String language = "en"; // In english.
    final String noticeXmlFilename = "pin-buyer_24_minimal";
    testGenerateHtmlFromFile(language, noticeXmlFilename);
  }

  @Test
  public void testEfxToHtmlX02FromString() throws IOException {
    final String language = "en"; // In english.
    final String noticeXmlFilename = "X02_registration";
    testGenerateHtmlFromString(language, noticeXmlFilename, "X02", SDK_VERSION);
  }

  private void testGenerateHtmlFromFile(final String language, final String noticeXmlName) {
    SdkResourcesLoader.getInstance().setRoot(SDK_RESOURCES_ROOT);

    Path noticeXmlPath = Path.of("src", "test", "resources", "xml", noticeXmlName + ".xml");

    final Optional<String> viewIdOpt = Optional.empty(); // Equivalent to not passing any in cli.
    final Path path = NoticeViewer.generateHtmlForUnitTest(language, noticeXmlPath, viewIdOpt);
    logger.info("TEST: Wrote html file: {}", path);
    final File htmlFile = path.toFile();
    assertTrue(htmlFile.exists());
    // The test would have failed if there were errors, this is what the check is really about.
  }

  private void testGenerateHtmlFromString(final String language, final String noticeXmlName,
      final String viewId, final String sdkVersion) throws IOException {
    SdkResourcesLoader.getInstance().setRoot(SDK_RESOURCES_ROOT);

    final Charset charsetUtf8 = StandardCharsets.UTF_8;
    final Path noticeXmlPath = Path.of("src", "test", "resources", "xml", noticeXmlName + ".xml");
    final String noticeXmlContent = Files.readString(noticeXmlPath, charsetUtf8);
    final Path xslPath = NoticeViewer.buildXsl(viewId, sdkVersion);
    final String xslContent = Files.readString(xslPath, charsetUtf8);
    final String html = NoticeViewer.generateHtmlForUnitTest(language, noticeXmlContent, xslContent,
        charsetUtf8, Optional.of(viewId));
    logger.info("TEST: Wrote html {} ...",  StringUtils.left(html, 50));
    assertTrue(StringUtils.isNotBlank(html));
    // The test would have failed if there were errors, this is what the check is really about.
  }

  @Test
  public void testEfxToXsl() throws IOException {
    final String viewId = "X02";
    final Path xsl = NoticeViewer.buildXsl(viewId, SDK_VERSION);
    logger.info("TEST: Wrote file: {}", xsl);
    assertTrue(xsl.toFile().exists());
    // The test would have failed if there were errors, this is what the check is really about.
  }
}
