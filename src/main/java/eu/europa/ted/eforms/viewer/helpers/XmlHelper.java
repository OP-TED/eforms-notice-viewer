package eu.europa.ted.eforms.viewer.helpers;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      throw new RuntimeException(MessageFormat.format("Failed to format XML:\n{0}", xmlString), e);
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
}
