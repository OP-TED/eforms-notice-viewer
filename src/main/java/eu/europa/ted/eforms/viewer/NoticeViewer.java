package eu.europa.ted.eforms.viewer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.viewer.helpers.SdkConstants;
import eu.europa.ted.eforms.viewer.helpers.ResourceLoader;
import eu.europa.ted.eforms.viewer.helpers.SafeDocumentBuilder;
import eu.europa.ted.efx.EfxTemplateTranslator;

public class NoticeViewer {
  private static final Logger logger = LoggerFactory.getLogger(NoticeViewer.class);

  public static Path generateHtmlForUnitTest(final String language, final String noticeXmlFilename,
      final Optional<String> viewIdOpt) {
    try {
      return generateHtml(language, noticeXmlFilename, viewIdOpt);
    } catch (Exception e) {
      throw new RuntimeException(e.toString(), e);
    }
  }

  /**
   * @param language The language as a two letter code
   * @param noticeXmlFilename The notice xml filename but without the xml extension
   * @param viewIdOpt An optional SDK view id to use, this can be used to enforce a custom view like
   *        notice summary. It could fail if this custom view is not compatible with the notice sub
   *        type
   * @return The path of the generated HTML file
   *
   * @throws IOException If an error occurs during input or output
   * @throws ParserConfigurationException Error related to XML reader configuration
   * @throws SAXException XML parse related errors
   */
  public static Path generateHtml(final String language, final String noticeXmlFilename,
      final Optional<String> viewIdOpt)
      throws IOException, SAXException, ParserConfigurationException {

    final Path noticeXmlPath = getNoticeXmlPath(noticeXmlFilename);
    logger.info("noticeName={}, noticeXmlPath={}", noticeXmlFilename, noticeXmlPath);
    assert noticeXmlPath != null : "Invalid path to notice: " + noticeXmlFilename;
    assert noticeXmlPath.toFile().exists() : "No such file: " + noticeXmlPath;

    final DocumentBuilder db = SafeDocumentBuilder.buildSafeDocumentBuilderStrict();
    final Document doc = db.parse(noticeXmlPath.toFile());
    doc.getDocumentElement().normalize();
    Element root = doc.getDocumentElement();

    // Find the corresponding notice sub type inside the XML.
    final Optional<String> noticeSubTypeFromXmlOpt = getNoticeSubType(root);
    if (noticeSubTypeFromXmlOpt.isEmpty()) {
      throw new RuntimeException(
          String.format("SubTypeCode not found in notice xml: %s", noticeXmlPath));
    }

    // Find the eForms SDK version inside the XML.
    final Optional<String> eformsSdkVersionOpt = getEformsSdkVersion(root);
    if (eformsSdkVersionOpt.isEmpty()) {
      throw new RuntimeException(
          String.format("eForms SDK version not found in notice xml: %s", noticeXmlPath));
    }

    // Build XSL from EFX.
    final String noticeSubType = noticeSubTypeFromXmlOpt.get();
    final String viewId = viewIdOpt.isPresent() ? viewIdOpt.get() : noticeSubType;

    final String eformsSdkVersion = eformsSdkVersionOpt.get();
    logger.info("noticeSubType={}, viewId={}, eformsSdkVersion={}", noticeSubType, viewId,
        eformsSdkVersion);

    final Path xslPath = NoticeViewer.buildXsl(viewId, eformsSdkVersion);
    logger.info("Created xsl file: {}", xslPath);

    return applyXslTransform(language, noticeXmlPath, xslPath, viewId);
  }

  static Path applyXslTransform(final String language, final Path noticeXmlPath, final Path xslPath,
      final String viewId) throws IOException {

    // https://www.saxonica.com/documentation11/#!using-xsl/embedding
    // Use Saxon HE so that we can evaluate XSL 2.0:
    System.setProperty("javax.xml.transform.TransformerFactory",
        "net.sf.saxon.TransformerFactoryImpl"); // Use the "net.sf.saxon" we have in the pom.xml

    try (InputStream inputStream = Files.newInputStream(xslPath)) {

      // XSL for input transformation.
      final TransformerFactory factory = TransformerFactory.newInstance();

      // SECURITY SETUP.
      // https://stackoverflow.com/questions/40649152/how-to-prevent-xxe-attack
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

      factory.setURIResolver(new CustomUriResolver());

      final Source xslSource = new StreamSource(inputStream);
      final Transformer transformer = factory.newTransformer(xslSource);
      // transformer.setURIResolver(uriResolver); Already set by the factory!

      // Parameters.
      // TODO use language in XsltUriResolver or pass it to transformer?
      transformer.setParameter("languageAsParam", language); // For en.xml or fr.xml, ...

      // HTML as output of the transformation.
      final Path outFolder = Path.of("target", "output-html");
      Files.createDirectories(outFolder);
      final Path htmlPath = outFolder.resolve(viewId + ".html");

      // XML as input.
      final Source xmlInput = new StreamSource(noticeXmlPath.toFile());
      transformer.transform(xmlInput, new StreamResult(htmlPath.toFile()));

      return htmlPath;

    } catch (TransformerFactoryConfigurationError | TransformerException e) {
      throw new RuntimeException(e.toString(), e);
    }
  }

  /**
   * Takes the EFX view template as a viewId string and outputs the XSL.
   *
   * @param viewId Something like "1" or "X02", it will try to get the corresponding view template
   *        from SDK by using naming conventions
   * @param sdkVersion The version of the desired SDK
   * @return Path to the built file
   * @throws IOException If an error occurred while writing the file
   */
  public static final Path buildXsl(final String viewId, final String sdkVersion)
      throws IOException {
    final Path viewPath = getPathToEfxAsStr(viewId);
    assert viewPath.toFile().exists() : "No such file: " + viewId;

    final String translation =
        EfxTemplateTranslator.renderTemplateFile(viewPath, sdkVersion, new DependencyFactory());

    final Path outFolder = Path.of("target", "output-xsl");
    Files.createDirectories(outFolder);
    final Path filePath = outFolder.resolve(viewId + ".xsl");
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
      writer.write(translation);
      writer.close();
    }
    return filePath;
  }

  /**
   * @param root The root element of the XML document
   * @return The eforms SDK version as found in the notice xml
   */
  public static Optional<String> getEformsSdkVersion(final Element root) {
    final NodeList nodeList = root.getElementsByTagName("cbc:CustomizationID");
    // We assume that length equals 1 exactly. Anything else is considered empty.
    return nodeList.getLength() == 1 ? Optional.of(nodeList.item(0).getTextContent().strip())
        : Optional.empty();
  }

  /**
   * @param root The root element of the XML document
   * @return The notice sub type as found in the notice xml
   */
  public static Optional<String> getNoticeSubType(final Element root) {
    final NodeList subTypeCodes = root.getElementsByTagName("cbc:SubTypeCode");
    for (int i = 0; i < subTypeCodes.getLength(); i++) {
      final Node item = subTypeCodes.item(i);
      final NamedNodeMap attributes = item.getAttributes();
      if (attributes != null) {
        // final Node listNameAttr = attributes.getNamedItem("listName");
        // if (listNameAttr != null && "notice-subtype".equals(listNameAttr.getNodeValue())) {
        return Optional.of(item.getTextContent().strip());
        // }
      }
    }
    return Optional.empty();
  }

  private static Path getNoticeXmlPath(final String cmdLnNoticeXml) {
    final String resourcePath = SdkConstants.EFORMS_SDK_EXAMPLES_NOTICES
        .resolve(Path.of(cmdLnNoticeXml + ".xml")).toString();
    return ResourceLoader.getResourceAsPath(resourcePath);
  }

  /**
   * @param viewId It can correspond to a view id, as long as there is one view id per notice id, or
   *        something else for custom views
   */
  public static Path getPathToEfxAsStr(final String viewId) {
    final String resourcePath = SdkConstants.EFORMS_SDK_NOTICE_TYPES_VIEW_TEMPLATES
        .resolve(Path.of(viewId + ".efx")).toString();
    return ResourceLoader.getResourceAsPath(resourcePath);
  }

}
