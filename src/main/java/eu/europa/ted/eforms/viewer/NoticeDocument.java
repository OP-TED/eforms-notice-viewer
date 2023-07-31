package eu.europa.ted.eforms.viewer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathNodes;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.viewer.util.xml.XmlHelper;

/**
 * A class representing a Notice document with accessor methods for its XML contents and metadata.
 */
public class NoticeDocument {
  private static final String TAG_PRIMARY_LANGUAGE = "cbc:NoticeLanguageCode";
  private static final String TAG_SDK_VERSION = "cbc:CustomizationID";
  private static final String TAG_SUBTYPE_CODE = "cbc:SubTypeCode";

  private static final String XPATH_ADDITIONAL_LANGUAGE = "/*/AdditionalNoticeLanguage/ID/text()";

  private static final XPath xpath;
  private final Element root;
  private final String xmlContents;

  static {
    xpath = XPathFactory.newInstance().newXPath();
  }

  public NoticeDocument(InputStream noticeXmlInput)
      throws ParserConfigurationException, SAXException, IOException {
    this(noticeXmlInput, null);
  }

  public NoticeDocument(InputStream noticeXmlInput, Charset charset)
      throws ParserConfigurationException, SAXException, IOException {
    this(IOUtils.toString(noticeXmlInput, charset));
  }

  public NoticeDocument(Path noticeXmlPath)
      throws ParserConfigurationException, SAXException, IOException {
    this(noticeXmlPath, null);
  }

  public NoticeDocument(final Path noticeXmlPath, Charset charset)
      throws ParserConfigurationException, SAXException, IOException {
    this(readXmlContents(noticeXmlPath, charset), charset);
  }

  public NoticeDocument(String noticeXmlContents)
      throws ParserConfigurationException, SAXException, IOException {
    this(noticeXmlContents, null);
  }

  public NoticeDocument(final String noticeXmlContents, Charset charset)
      throws ParserConfigurationException, SAXException, IOException {
    Validate.notBlank(noticeXmlContents, "Invalid Notice XML contents");
    this.xmlContents = noticeXmlContents;

    charset = ObjectUtils.defaultIfNull(charset, NoticeViewerConstants.DEFAULT_CHARSET);

    this.root = XmlHelper.getXmlRoot(noticeXmlContents, charset);
    Validate.notNull(this.root, "No XML root found");
  }

  private static String readXmlContents(final Path noticeXmlPath, Charset charset)
      throws IOException {
    Validate.notNull(noticeXmlPath, "Undefined Notice XML file path");

    if (!Files.isRegularFile(noticeXmlPath)) {
      throw new FileNotFoundException(noticeXmlPath.toString());
    }

    charset = ObjectUtils.defaultIfNull(charset, NoticeViewerConstants.DEFAULT_CHARSET);

    return Files.readString(noticeXmlPath, charset);
  }

  /**
   * @return The notice sub type as found in the notice XML
   */
  public String getNoticeSubType() {
    return Optional.ofNullable(root.getElementsByTagName(TAG_SUBTYPE_CODE))
        .map((NodeList subTypeCodes) -> {
          Optional<String> result = Optional.empty();

          for (int i = 0; i < subTypeCodes.getLength(); i++) {
            result = Optional.ofNullable(subTypeCodes.item(i))
                .filter((Node node) -> node.getAttributes() != null)
                .map(Node::getTextContent)
                .map(StringUtils::strip);
          }

          return result.orElse(null);
        })
        .filter(StringUtils::isNotBlank)
        .orElseThrow(() -> new RuntimeException("SubTypeCode not found in notice XML"));
  }

  /**
   * @return The eforms SDK version as found in the notice XML
   */
  public String getEformsSdkVersion() {
    // We assume that length equals 1 exactly. Anything else is considered
    // empty.
    return Optional.ofNullable(root.getElementsByTagName(TAG_SDK_VERSION))
        .filter((NodeList nodes) -> nodes.getLength() == 1)
        .map((NodeList nodes) -> Optional.ofNullable(nodes.item(0))
            .map(Node::getTextContent)
            .map(StringUtils::strip)
            .map((String str) -> StringUtils.removeStart(str, "eforms-sdk-"))
            .orElse(null))
        .filter(StringUtils::isNotBlank)
        .orElseThrow(() -> new RuntimeException("eForms SDK version not found in notice XML"));
  }

  /**
   * @return The primary language
   */
  public String getPrimaryLanguage() {
    return Optional
        .ofNullable(root.getElementsByTagName(TAG_PRIMARY_LANGUAGE))
        .map((NodeList nodes) -> nodes.item(0))
        .map(Node::getTextContent)
        .orElse(null);
  }

  /**
   * @return A list of other languages
   * @throws XPathExpressionException
   */
  public List<String> getOtherLanguages() throws XPathExpressionException {
    return Optional
        .ofNullable(xpath.evaluateExpression(XPATH_ADDITIONAL_LANGUAGE, root.getOwnerDocument(),
            XPathNodes.class))
        .map((XPathNodes nodes) -> {
          final List<String> languages = new ArrayList<>();

          nodes.forEach((Node node) -> {
            if (StringUtils.isNotBlank(node.getTextContent())) {
              languages.add(node.getTextContent());
            }
          });

          return languages;
        })
        .orElseGet(ArrayList<String>::new);
  }

  /**
   * @return The notice XML
   */
  public String getXmlContents() {
    return xmlContents;
  }
}
