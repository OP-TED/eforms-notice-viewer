package eu.europa.ted.eforms.viewer.util.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.Validate;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XmlHelper {
  private static final Logger logger = LoggerFactory.getLogger(XmlHelper.class);

  public static final int DEFAULT_INDENT = 4;

  private XmlHelper() {}

  public static String formatXml(String xmlString) {
    return formatXml(xmlString, DEFAULT_INDENT, true);
  }

  public static String formatXml(String xmlString, boolean skipDeclaration) {
    return formatXml(xmlString, DEFAULT_INDENT, skipDeclaration);
  }

  private static OutputFormat getOutputFormat(int indent, boolean skipDeclaration) {
    OutputFormat format = OutputFormat.createPrettyPrint();
    format.setIndentSize(indent);
    format.setSuppressDeclaration(skipDeclaration);

    return format;
  }

  public static String formatXml(String xmlString, int indent, boolean skipDeclaration) {
    logger.debug("Formatting XML");
    logger.trace("XML Input:\n{}", xmlString);

    XMLWriter xmlWriter = null;
    try (StringWriter sw = new StringWriter()) {
      Document document = DocumentHelper.parseText(xmlString);

      xmlWriter = new XMLWriter(sw, getOutputFormat(indent, skipDeclaration));
      xmlWriter.write(document);

      return sw.toString();
    } catch (Exception e) {
      logger.error(MessageFormat.format("Failed to format XML:\n{0}", xmlString), e);
      return xmlString;
    } finally {
      if (xmlWriter != null) {
        try {
          xmlWriter.close();
        } catch (IOException e) {
          logger.debug("Error closing XML writer", e);
        }
      }
    }
  }

  public static Element getXmlRoot(InputStream xmlContent)
      throws ParserConfigurationException, SAXException, IOException {
    Validate.notNull(xmlContent, "Undefined XML content.");

    final org.w3c.dom.Document document = getDocumentBuilder().parse(xmlContent);
    return getDocumentRoot(document);
  }

  public static Element getXmlRoot(Path xmlPath)
      throws ParserConfigurationException, SAXException, IOException {
    Validate.notNull(xmlPath, "Undefined XML path.");

    if (!Files.isRegularFile(xmlPath)) {
      throw new FileNotFoundException(xmlPath.toString());
    }

    final org.w3c.dom.Document document = getDocumentBuilder().parse(xmlPath.toFile());

    return getDocumentRoot(document);
  }

  public static Element getDocumentRoot(org.w3c.dom.Document document) {
    Validate.notNull(document, "Undefined document");

    document.getDocumentElement().normalize();
    return document.getDocumentElement();
  }

  public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
    return SafeDocumentBuilder.buildSafeDocumentBuilderStrict();
  }
}
