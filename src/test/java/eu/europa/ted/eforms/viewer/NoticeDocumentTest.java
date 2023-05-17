package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

class NoticeDocumentTest {
  @Test
  void testNoticeLocalesExtraction()
      throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    final NoticeDocument document =
        new NoticeDocument(
            Path.of("src", "test", "resources", "xml", "1.0", "cn_24_multilingual.xml"));

    assertEquals("ENG", document.getPrimaryLanguage());

    assertEquals(Arrays.asList("BUL", "CES"),
        document.getOtherLanguages());
  }
}
