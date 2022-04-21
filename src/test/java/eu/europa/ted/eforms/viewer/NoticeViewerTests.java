package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.viewer.helpers.SdkConstants;
import eu.europa.ted.eforms.viewer.helpers.ResourceLoader;

public class NoticeViewerTests {

  private static final Logger logger = LoggerFactory.getLogger(NoticeViewerTests.class);

  private static final String SDK_VERSION = "latest";

  @Test
  @SuppressWarnings("static-method")
  public void testEfxToXsl() throws IOException {
    final String viewId = "X02";
    final Path xsl = NoticeViewer.buildXsl(viewId, SDK_VERSION);
    logger.info("Wrote file: {}", xsl);
    assertTrue(xsl.toFile().exists());
  }

  /**
   * A minimal test covering an XML to HTML transformation using XSL in isolation. This can be used
   * to quickly try out things with XSL.
   */
  @Test
  @SuppressWarnings("static-method")
  public void testXslToXml() throws IOException {
    final String language = "en";
    final String viewId = "X02";
    final Path noticeXmlPath = ResourceLoader.getResourceAsPath(
        SdkConstants.EFORMS_SDK_EXAMPLES_NOTICES.resolve("X02_registration.xml").toString());
    final Path xslPath = ResourceLoader.getResourceAsPath(Path.of("xsl", "X02.xsl").toString());
    final Path html = NoticeViewer.applyXslTransform(language, noticeXmlPath, xslPath, viewId);
    logger.info("Wrote file: {}", html);
    assertTrue(html.toFile().exists(), "Expecting HTML file to exist.");
  }

  @Test
  @SuppressWarnings("static-method")
  public void testEfxToHtmlFull() {
    final String language = "en";
    final String noticeXmlFilename = "X02_registration";
    final Path xsl =
        NoticeViewer.generateHtmlForUnitTest(language, noticeXmlFilename, Optional.empty());
    logger.info("Wrote file: {}", xsl);
    assertTrue(xsl.toFile().exists());
  }
}
