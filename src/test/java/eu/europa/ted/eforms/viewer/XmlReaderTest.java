package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.junit.jupiter.api.Test;
import eu.europa.ted.eforms.viewer.helpers.JavaTools;
import eu.europa.ted.eforms.viewer.reader.XmlReader;
import eu.europa.ted.eforms.viewer.reader.XmlReaderFactoryImpl;

@SuppressWarnings("static-method")
public class XmlReaderTest {

  private static final String X02_REGISTRATION_XML = "X02_registration.xml";
  private static final String NOTICES_FOLDER = "notices/";

  @Test
  public void testXpathSingle() throws XPathExpressionException {
    final XmlReader xmlReader = xmlReaderForX02Registration();

    final String expected = "The registrant declares ...";
    final String xpathExpression = "/*/cbc:Note";

    assertEquals(expected, xmlReader.valuesOf(xpathExpression).get(0));

    // Read again to ensure no input stream was not closed prematurely in the reader implementation.
    assertEquals(expected, xmlReader.valuesOf(xpathExpression).get(0));
  }

  @Test
  public void testXpathMany() throws XPathExpressionException {
    final XmlReader xmlReader = xmlReaderForX02Registration();

    final List<String> expected = new ArrayList<>(Arrays.asList("education", "health", "soc-pro"));
    final String xpathExpression = "/*/cac:BusinessCapability/cbc:CapabilityTypeCode";

    assertEquals(expected, xmlReader.valuesOf(xpathExpression));

    // Read again to ensure no input stream was not closed prematurely in the reader implementation.
    assertEquals(expected, xmlReader.valuesOf(xpathExpression));
  }

  @Test
  public void testXpathWithContext() throws XPathExpressionException {
    final XmlReader xmlReader = xmlReaderForX02Registration();

    final String expected = "DUMMY";
    final String contextXpath = "/*/cac:AdditionalDocumentReference";
    final String relativeXpath = "cbc:ID";

    assertEquals(expected, xmlReader.valuesOf(relativeXpath, contextXpath).get(0));

    // Read again to ensure no input stream was not closed prematurely in the reader implementation.
    assertEquals(expected, xmlReader.valuesOf(relativeXpath, contextXpath).get(0));
  }

  private XmlReader xmlReaderForX02Registration() {
    final String noticeXml = NOTICES_FOLDER + X02_REGISTRATION_XML;
    final Path path = JavaTools.getResourceAsPath(noticeXml);
    return new XmlReaderFactoryImpl().create(path);
  }

}
