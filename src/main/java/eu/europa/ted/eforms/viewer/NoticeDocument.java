package eu.europa.ted.eforms.viewer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
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
  private final Element root;
  private final String xmlContents;

  public NoticeDocument(InputStream noticeXmlInput)
      throws ParserConfigurationException, SAXException, IOException {
    this(noticeXmlInput, null);
  }

  public NoticeDocument(InputStream noticeXmlInput, Charset charset)
      throws ParserConfigurationException, SAXException, IOException {
    this(IOUtils.toString(noticeXmlInput,
        ObjectUtils.defaultIfNull(charset, NoticeViewerConstants.DEFAULT_CHARSET)));
  }

  public NoticeDocument(String noticeXmlContents)
      throws ParserConfigurationException, SAXException, IOException {
    this(noticeXmlContents, null);
  }

  public NoticeDocument(final String noticeXmlContents, Charset charset)
      throws ParserConfigurationException, SAXException, IOException {
    Validate.notBlank(noticeXmlContents, "Invalid Notice XML contents");

    charset = ObjectUtils.defaultIfNull(charset, NoticeViewerConstants.DEFAULT_CHARSET);
    this.root = XmlHelper.getXmlRoot(noticeXmlContents, charset);
    this.xmlContents = noticeXmlContents;
  }

  public NoticeDocument(Path noticeXmlPath)
      throws ParserConfigurationException, SAXException, IOException {
    this(noticeXmlPath, null);
  }

  public NoticeDocument(final Path noticeXmlPath, Charset charset)
      throws ParserConfigurationException, SAXException, IOException {
    Validate.notNull(noticeXmlPath, "Undefined Notice XML file path");

    if (!Files.isRegularFile(noticeXmlPath)) {
      throw new FileNotFoundException(noticeXmlPath.toString());
    }

    charset = ObjectUtils.defaultIfNull(charset, NoticeViewerConstants.DEFAULT_CHARSET);

    this.root = XmlHelper.getXmlRoot(noticeXmlPath);
    this.xmlContents = Files.readString(noticeXmlPath, charset);
  }

  /**
   * @return The notice sub type as found in the notice XML
   */
  public String getNoticeSubType() {
    return Optional.ofNullable(root)
        .map((Element element) -> element.getElementsByTagName("cbc:SubTypeCode"))
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
    return Optional.ofNullable(root)
        .map((Element element) -> element.getElementsByTagName("cbc:CustomizationID"))
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
   * @return The notice XML
   */
  public String getXmlContents() {
    return xmlContents;
  }
}
