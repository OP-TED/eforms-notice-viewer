package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class EfxToXslTranslatorTests {
  private static final String SDK_VERSION = "latest";

  @Test
  @SuppressWarnings("static-method")
  public void testTranslateFile() throws IOException {
    final String viewTemplateId = "X02";
    final Path xslPath = NoticeViewer.buildXsl(viewTemplateId, SDK_VERSION);
    assertTrue(xslPath.toFile().exists());
    // TODO check the XSL and add some junit assertions.
  }

}
