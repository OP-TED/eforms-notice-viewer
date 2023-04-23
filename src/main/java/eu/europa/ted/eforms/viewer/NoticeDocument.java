package eu.europa.ted.eforms.viewer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.viewer.util.xml.XmlHelper;

public class NoticeDocument {
  private final Element root;

  public NoticeDocument(InputStream noticeXmlContent)
      throws ParserConfigurationException, SAXException, IOException {
    this.root = XmlHelper.getXmlRoot(noticeXmlContent);
  }

  public NoticeDocument(Path noticeXmlPath)
      throws ParserConfigurationException, SAXException, IOException {
    this.root = XmlHelper.getXmlRoot(noticeXmlPath);
  }

  /**
   * @return The notice sub type as found in the notice xml
   */
  public String getNoticeSubType() {
    final NodeList subTypeCodes = root.getElementsByTagName("cbc:SubTypeCode");

    String noticeSubType = StringUtils.EMPTY;

    for (int i = 0; i < subTypeCodes.getLength(); i++) {
      final Node item = subTypeCodes.item(i);
      final NamedNodeMap attributes = item.getAttributes();

      if (attributes != null) {
        noticeSubType = item.getTextContent().strip();
      }
    }

    if (StringUtils.isBlank(noticeSubType)) {
      throw new RuntimeException("SubTypeCode not found in notice XML");
    }

    return noticeSubType;
  }

  /**
   * @return The eforms SDK version as found in the notice xml
   */
  public String getEformsSdkVersion() {
    final NodeList nodeList = root.getElementsByTagName("cbc:CustomizationID");

    // We assume that length equals 1 exactly. Anything else is considered
    // empty.
    final String sdkVersion = nodeList.getLength() == 1
        ? StringUtils.removeStart(nodeList.item(0).getTextContent().strip(), "eforms-sdk-")
        : StringUtils.EMPTY;

    if (StringUtils.isBlank(sdkVersion)) {
      throw new RuntimeException("eForms SDK version not found in notice XML");
    }

    return sdkVersion;
  }
}
