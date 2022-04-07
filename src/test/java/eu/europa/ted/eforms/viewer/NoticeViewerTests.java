package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.viewer.helpers.ResourceLoader;

public class NoticeViewerTests {
  private static final Logger logger = LoggerFactory.getLogger(NoticeViewerTests.class);

  /**
   * A minimal test covering an XSL transformation in isolation.
   */
  @Test
  @SuppressWarnings("static-method")
  public void testXslPartOnly() throws IOException {
    final String language = "en";
    final String viewId = "X02";
    final Path noticeXmlPath =
        ResourceLoader.getResourceAsPath("eforms-sdk/examples/notices/X02_registration.xml");
    final Path xslPath = ResourceLoader.getResourceAsPath("xsl/X02.xsl");
    final Path path = NoticeViewer.applyXslTransform(language, noticeXmlPath, xslPath, viewId);
    logger.info("Wrote file: {}", path);
    assertTrue(path.toFile().exists(), "Expecting HTML file to exist.");
  }
}
