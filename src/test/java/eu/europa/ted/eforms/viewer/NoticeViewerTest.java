package eu.europa.ted.eforms.viewer;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import eu.europa.ted.eforms.viewer.helpers.ResourceLoader;

public class NoticeViewerTest {

  @Test
  @SuppressWarnings("static-method")
  public void testXslIndependently() throws IOException {
    final String language = "en";
    final String viewId = "X02";
    final Path noticeXmlPath =
        ResourceLoader.getResourceAsPath("eforms-sdk/examples/notices/X02_registration.xml");
    final Path xslPath = ResourceLoader.getResourceAsPath("xsl/X02.xsl");
    NoticeViewer.applyXslTransform(language, noticeXmlPath, xslPath, viewId);
  }
}
