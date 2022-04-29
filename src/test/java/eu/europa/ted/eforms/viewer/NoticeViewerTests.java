package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("static-method")
public class NoticeViewerTests {

  private static final Logger logger = LoggerFactory.getLogger(NoticeViewerTests.class);

  private static final String SDK_VERSION = "latest";

  @Test
  public void testEfxToHtmlX01English() {
    final String language = "en"; // In english.
    final String noticeXmlFilename = "X01_EEIG"; // "X01_EEIG.xml"
    testGenerateHtml(language, noticeXmlFilename);
  }

  @Test
  public void testEfxToHtmlX01Greek() {
    final String language = "el"; // In greek.
    final String noticeXmlFilename = "X01_EEIG"; // "X01_EEIG.xml"
    testGenerateHtml(language, noticeXmlFilename);
  }

  @Test
  public void testEfxToHtmlX02() {
    final String language = "en"; // In english.
    final String noticeXmlFilename = "X02_registration";
    testGenerateHtml(language, noticeXmlFilename);
  }

  @Test
  public void testEfxToHtml1Minimal() {
    final String language = "en"; // In english.
    final String noticeXmlFilename = "pin-buyer_24_minimal";
    testGenerateHtml(language, noticeXmlFilename);
  }

  private void testGenerateHtml(final String language, final String noticeXmlFilename) {
    final Optional<String> viewIdOpt = Optional.empty(); // Equivalent to not passing any in cli.
    final Path path = NoticeViewer.generateHtmlForUnitTest(language, noticeXmlFilename, viewIdOpt);
    logger.info("TEST: Wrote html file: {}", path);
    final File htmlFile = path.toFile();
    assertTrue(htmlFile.exists());
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
