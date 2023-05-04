package eu.europa.ted.eforms.viewer.util.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Utility class with methods for parsing and handling XML
 *
 */
public class XmlHelper {
  private static final Logger logger = LoggerFactory.getLogger(XmlHelper.class);

  public static final int DEFAULT_INDENT = 4;

  private XmlHelper() {}

  /**
   * Formats (pretty-prints) XML
   *
   * @param xmlString The XML to format
   * @return Formatted XML
   * @throws DocumentException when the XML document cannot be parsed
   * @throws IOException When a error occurs when writing the result into a string
   */
  public static String formatXml(String xmlString) throws DocumentException, IOException {
    return formatXml(xmlString, DEFAULT_INDENT, true);
  }

  /**
   * Formats (pretty-prints) XML
   *
   * @param xmlString The XML to format
   * @param skipDeclaration If set, the XML declaration will be suppressed
   * @return Formatted XML
   * @throws DocumentException when the XML document cannot be parsed
   * @throws IOException When a error occurs when writing the result into a string
   */
  public static String formatXml(String xmlString, boolean skipDeclaration)
      throws DocumentException, IOException {
    return formatXml(xmlString, DEFAULT_INDENT, skipDeclaration);
  }

  /**
   * Formats (pretty-prints) XML
   *
   * @param xmlString The XML to format
   * @param indent The indentation size (in spaces) to use for XML formatting
   * @param skipDeclaration If set, the XML declaration will be suppressed when formatting XML
   * @return Formatted XML
   * @throws DocumentException when the XML document cannot be parsed
   * @throws IOException When a error occurs when writing the result into a string
   */
  public static String formatXml(final String xmlString, final int indent,
      final boolean skipDeclaration) throws DocumentException, IOException {
    if (StringUtils.isBlank(xmlString)) {
      return StringUtils.EMPTY;
    }

    logger.debug("Formatting XML");
    logger.trace("XML Input:\n{}", xmlString);

    XMLWriter xmlWriter = null;

    try (StringWriter sw = new StringWriter()) {
      Document document = DocumentHelper.parseText(xmlString);

      xmlWriter = new XMLWriter(sw, getOutputFormat(indent, skipDeclaration));
      xmlWriter.write(document);

      return sw.toString();
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

  /**
   * Creates a configured {@link OutputFormat} instance
   *
   * @param indent The indentation size (in spaces) to use for XML formatting
   * @param skipDeclaration If set, the XML declaration will be suppressed when formatting XML
   * @return A configured {@link OutputFormat} instance
   */
  private static OutputFormat getOutputFormat(int indent, boolean skipDeclaration) {
    OutputFormat format = OutputFormat.createPrettyPrint();

    format.setIndentSize(indent);
    format.setSuppressDeclaration(skipDeclaration);

    return format;
  }

  /**
   * Retrieves the root element of a XML file
   *
   * @param xmlPath The path to the target XML file
   * @return A {@link Element} representing the XML root
   * @throws IOException when any IO errors occur
   * @throws ParserConfigurationException when an error occurs during configuration of the XML
   *         parser
   * @throws SAXException when any parse errors occur.
   */
  public static Element getXmlRoot(final Path xmlPath)
      throws IOException, SAXException, ParserConfigurationException {
    Validate.notNull(xmlPath, "Undefined XML path.");

    if (!Files.isRegularFile(xmlPath)) {
      throw new FileNotFoundException(xmlPath.toString());
    }

    try (InputStream xmlInput = Files.newInputStream(xmlPath)) {
      Validate.notNull(xmlInput, "XML input stream for file [%s] is null", xmlPath);

      return getXmlRoot(xmlInput);
    }
  }

  /**
   * Retrieves the root element of a XML document provided as a string
   *
   * @param xmlContents The string containing the XML document's contents
   * @param charset The character set to use for parsing
   * @return A {@link Element} representing the XML root
   * @throws IOException when any IO errors occur
   * @throws ParserConfigurationException when an error occurs during configuration of the XML
   *         parser
   * @throws SAXException when any parse errors occur.
   */
  public static Element getXmlRoot(final String xmlContents, final Charset charset)
      throws IOException, SAXException, ParserConfigurationException {
    Validate.notBlank(xmlContents, "Empty XML.");

    try (InputStream xmlInput = IOUtils.toInputStream(xmlContents, charset)) {
      Validate.notNull(xmlInput, "XML input stream is null");

      return getXmlRoot(xmlInput);
    }
  }

  /**
   * Retrieves the root element of a XML document provided by an input stream
   *
   * @param xmlInput The input stream providing the XML document's contents
   * @return A {@link Element} representing the XML root
   * @throws ParserConfigurationException when an error occurs during configuration of the XML
   *         parser
   * @throws IOException when any IO errors occur.
   * @throws SAXException when any parse errors occur.
   */
  public static Element getXmlRoot(final InputStream xmlInput)
      throws SAXException, IOException, ParserConfigurationException {
    Validate.notNull(xmlInput, "Undefined XML input.");

    final org.w3c.dom.Document document = getDocumentBuilder().parse(xmlInput);

    return Optional
        .ofNullable(document)
        .map(XmlHelper::getDocumentRoot)
        .orElse(null);
  }

  /**
   * Retrieves the root of an XML represented by a {@link org.w3c.dom.Document} instance
   *
   * @param document The {@link org.w3c.dom.Document} instance
   * @return A {@link Element} representing the XML root
   */
  public static Element getDocumentRoot(org.w3c.dom.Document document) {
    Validate.notNull(document, "Undefined document");

    document.getDocumentElement().normalize();

    return document.getDocumentElement();
  }

  /**
   * Creates a {@link DocumentBuilder} instance for XML parsing
   *
   * @return A configured {@link DocumentBuilder} instance
   * @throws ParserConfigurationException when the builder is configured with a feature that is
   *         unsupported by the XML processor
   */
  public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
    return SafeDocumentBuilder.buildSafeDocumentBuilderStrict();
  }
}
