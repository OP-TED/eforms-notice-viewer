package eu.europa.ted.eforms.viewer.reader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * DOM based XmlReader implementation.
 */
public class XmlReaderDomImpl implements XmlReader {
  private static final Map<String, String> NS = new HashMap<>();
  static {
    NS.put("cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
    NS.put("cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
    NS.put("efext", "http://data.europa.eu/p27/eforms-ubl-extensions/1");
    NS.put("efbc", "http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1");
    NS.put("ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
    NS.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    NS.put("fn", "http://www.w3.org/2005/xpath-functions");
  }

  private final Document xmlDoc;
  private final XPath xpath;

  public XmlReaderDomImpl(final Path pathToXmlDocument)
      throws ParserConfigurationException, SAXException, IOException {
    this(buildDefaultDoc(pathToXmlDocument));
  }

  /**
   * @param xmlDocument W3C Document (DOM)
   */
  public XmlReaderDomImpl(final Document xmlDocument) {
    this.xmlDoc = xmlDocument;
    this.xpath = setupDefaultXpath();
  }

  /**
   * @param xmlDocument W3C Document (DOM)
   * @param xpath An XPath object of your own choice, but keep in mind it is not thread-safe.
   */
  public XmlReaderDomImpl(final Document xmlDocument, final XPath xpath) {
    this.xmlDoc = xmlDocument;
    this.xpath = xpath;
  }

  /**
   * @return One or more String values resulting of evaluating the expression.
   */
  @Override
  public List<String> valuesOf(final String xpathExpression) throws XPathExpressionException {
    final NodeList nodes =
        (NodeList) this.xpath.evaluate(xpathExpression, xmlDoc, XPathConstants.NODESET);
    return getNodesTexts(nodes);
  }

  /**
   * @return One or more String values resulting of evaluating the expression. Implementation
   *         specific.
   */
  private List<String> valuesOf(final String xpathExpression, final Node contextNode)
      throws XPathExpressionException {
    final NodeList nodes =
        (NodeList) this.xpath.evaluate(xpathExpression, contextNode, XPathConstants.NODESET);
    return getNodesTexts(nodes);
  }

  @Override
  public List<String> valuesOf(final String relativeXpath, final String contextXpath)
      throws XPathExpressionException {
    final Node contextNode = (Node) this.xpath.evaluate(contextXpath, xmlDoc, XPathConstants.NODE);
    return valuesOf(relativeXpath, contextNode);
  }

  /**
   * @param path Path to the notice xml file
   * @return W3C Document (DOM).
   */
  private static Document buildDefaultDoc(final Path path)
      throws ParserConfigurationException, SAXException, IOException {

    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setCoalescing(false); // Use false, otherwise we have whitespace issues.
    dbf.setExpandEntityReferences(true);
    dbf.setIgnoringComments(true);
    dbf.setIgnoringElementContentWhitespace(false);
    dbf.setNamespaceAware(true); // Yes as we have many namespaces in our xpath expressions.
    dbf.setValidating(false);

    final DocumentBuilder db = dbf.newDocumentBuilder();
    final Document doc = db.parse(path.toFile());
    assert StandardCharsets.UTF_8.toString().equalsIgnoreCase(doc.getXmlEncoding());
    doc.setXmlStandalone(true);
    doc.setStrictErrorChecking(false);

    return doc;
  }

  private static XPath setupDefaultXpath() {
    // NOTE: this object is not thread-safe, this is why we rebuild it each time.
    final XPath newXPath = XPathFactory.newInstance().newXPath();
    newXPath.setNamespaceContext(new NamespaceContext() {
      @Override
      public String getNamespaceURI(String arg0) {
        final String namespace = NS.get(arg0);
        if (namespace != null) {
          return namespace;
        }
        return XMLConstants.NULL_NS_URI;
      }

      @Override
      public String getPrefix(String arg0) {
        return null;
      }

      @Override
      public Iterator<String> getPrefixes(String arg0) {
        return null;
      }
    });
    return newXPath;
  }

  private static List<String> getNodesTexts(final NodeList nodes) {
    final int length = nodes.getLength();
    final List<String> texts = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      texts.add(nodes.item(i).getTextContent());
    }
    return texts;
  }

}
