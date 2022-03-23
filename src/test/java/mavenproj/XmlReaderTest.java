package mavenproj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import eu.europa.ted.efx.util.JavaTools;
import eu.europa.ted.reader.XmlReaderDomImpl;

@SuppressWarnings("static-method")
public class XmlReaderTest {

  private static final String X02_REGISTRATION_XML = "X02_registration.xml";
  private static final String NOTICES_FOLDER = "notices/";

  @Test
  public void testXmlReaderXpathSingle()
      throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
    final XmlReaderDomImpl xmlReader = xmlReaderForX02Registration();

    final String expected = "The registrant declares ...";
    final String xpathExpression = "/*/cbc:Note";

    assertEquals(expected, xmlReader.valuesOf(xpathExpression).get(0));

    // Read again to ensure no input stream was not closed prematurely in the reader implementation.
    assertEquals(expected, xmlReader.valuesOf(xpathExpression).get(0));
  }

  @Test
  public void testXmlReaderXpathRepeating()
      throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
    final XmlReaderDomImpl xmlReader = xmlReaderForX02Registration();

    final List<String> expected = new ArrayList<>(Arrays.asList("education", "health", "soc-pro"));
    final String xpathExpression = "/*/cac:BusinessCapability/cbc:CapabilityTypeCode";

    assertEquals(expected, xmlReader.valuesOf(xpathExpression));

    // Read again to ensure no input stream was not closed prematurely in the reader implementation.
    assertEquals(expected, xmlReader.valuesOf(xpathExpression));
  }

  private XmlReaderDomImpl xmlReaderForX02Registration()
      throws SAXException, IOException, ParserConfigurationException {
    final String noticeXml = NOTICES_FOLDER + X02_REGISTRATION_XML;
    final Path resourceAsPath = JavaTools.getResourceAsPath(noticeXml);

    // TODO use XmlReaderFactory interface

    return new XmlReaderDomImpl(resourceAsPath);
  }

}
