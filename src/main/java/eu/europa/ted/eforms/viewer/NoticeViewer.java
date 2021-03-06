package eu.europa.ted.eforms.viewer;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.selector.resource.SdkDownloader;
import eu.europa.ted.eforms.viewer.helpers.SafeDocumentBuilder;
import eu.europa.ted.eforms.viewer.helpers.CustomUriResolver;
import eu.europa.ted.eforms.viewer.helpers.SdkResourceLoader;
import eu.europa.ted.efx.EfxTranslator;

public class NoticeViewer {
  private static final Logger logger = LoggerFactory.getLogger(NoticeViewer.class);

  private NoticeViewer() {}

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
   * @throws InstantiationException 
   */
  public static Path generateHtml(final String language, final Path noticeXmlPath,
      final Optional<String> viewIdOpt)
      throws IOException, SAXException, ParserConfigurationException, InstantiationException {
    logger.info("noticeXmlPath={}", noticeXmlPath);
    Validate.notNull(noticeXmlPath, "Invalid path to notice: " + noticeXmlPath);
    Validate.isTrue(Files.isRegularFile(noticeXmlPath), "No such file: " + noticeXmlPath);
    final DocumentBuilder db = SafeDocumentBuilder.buildSafeDocumentBuilderStrict();
    final Document doc = db.parse(noticeXmlPath.toFile());
    doc.getDocumentElement().normalize();
    final Element root = doc.getDocumentElement();
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
    final Path htmlPath =
        applyXslTransform(language, eformsSdkVersion, noticeXmlPath, xslPath, viewId);
    // Ensure the HTML can be parsed.
    Jsoup.parse(htmlPath.toFile(), StandardCharsets.UTF_8.toString());
    return htmlPath;
  }

  /**
   * @param language The language as a two letter code
   * @param noticeXmlContent The notice xml content
   * @param xsl structure of the notice
   * @param charset of the input string content (xml notice and xsl structure) and output html
   *        string
   * @param viewIdOpt An optional SDK view id to use, this can be used to enforce a custom view like
   *        notice summary. It could fail if this custom view is not compatible with the notice sub
   *        type
   * @return The generated HTML string
   *
   * @throws IOException If an error occurs during input or output
   * @throws ParserConfigurationException Error related to XML reader configuration
   * @throws SAXException XML parse related errors
   */
  public static String generateHtml(final String language, final String noticeXmlContent,
      final String xsl, final Charset charset, final Optional<String> viewIdOpt)
      throws IOException, SAXException, ParserConfigurationException {
    logger.info("noticeXmlContent={} ...", StringUtils.left(noticeXmlContent, 50));
    Validate.notNull(noticeXmlContent, "Invalid notice content: " + noticeXmlContent);
    try (
        final ByteArrayInputStream noticeXmlInputStream =
            new ByteArrayInputStream(noticeXmlContent.trim().getBytes(charset));
        final ByteArrayInputStream xslInputStream =
            new ByteArrayInputStream(xsl.getBytes(charset));) {
      return generateHtml(language, noticeXmlInputStream, xslInputStream, charset, viewIdOpt);
    }
  }

  /**
   * @param language The language as a two letter code
   * @param noticeXmlContent The notice xml content as InputStream
   * @param xslIs structure of the notice as InputStream
   * @param charset of the output html string
   * @param viewIdOpt An optional SDK view id to use, this can be used to enforce a custom view like
   *        notice summary. It could fail if this custom view is not compatible with the notice sub
   *        type
   * @return The generated HTML string using the input charset
   *
   * @throws IOException If an error occurs during input or output
   * @throws ParserConfigurationException Error related to XML reader configuration
   * @throws SAXException XML parse related errors
   */
  public static String generateHtml(final String language,
      final ByteArrayInputStream noticeXmlContent, final ByteArrayInputStream xslIs,
      final Charset charset, final Optional<String> viewIdOpt)
      throws IOException, ParserConfigurationException, SAXException {
    try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
      noticeXmlContent.transferTo(baos);

      try (final InputStream noticeXmlIsClone1 = new ByteArrayInputStream(baos.toByteArray());
          final InputStream noticeXmlIsClone2 = new ByteArrayInputStream(baos.toByteArray());) {
        final DocumentBuilder db = SafeDocumentBuilder.buildSafeDocumentBuilderStrict();
        final Document doc = db.parse(noticeXmlIsClone1);

        doc.getDocumentElement().normalize();

        final Element root = doc.getDocumentElement();

        // Find the corresponding notice sub type inside the XML.
        final Optional<String> noticeSubTypeFromXmlOpt = getNoticeSubType(root);
        if (noticeSubTypeFromXmlOpt.isEmpty()) {
          throw new RuntimeException("SubTypeCode not found in notice xml");
        }

        // Find the eForms SDK version inside the XML.
        final Optional<String> eformsSdkVersionOpt = getEformsSdkVersion(root);
        if (eformsSdkVersionOpt.isEmpty()) {
          throw new RuntimeException("eForms SDK version not found in notice xml");
        }

        // Build XSL from EFX.
        final String noticeSubType = noticeSubTypeFromXmlOpt.get();
        final String viewId = viewIdOpt.isPresent() ? viewIdOpt.get() : noticeSubType;
        final String eformsSdkVersion = eformsSdkVersionOpt.get();
        logger.info("noticeSubType={}, viewId={}, eformsSdkVersion={}", noticeSubType, viewId,
            eformsSdkVersion);

        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
          final StreamResult htmlResult = new StreamResult(outputStream);

          applyXslTransform(language, eformsSdkVersion, new StreamSource(noticeXmlIsClone2),
              new StreamSource(xslIs), htmlResult);

          // Ensure the HTML can be parsed.
          final String htmlText = outputStream.toString(charset);
          Jsoup.parse(htmlText);

          return htmlText;
        }
      }
    }
  }

  static Path applyXslTransform(final String language, String sdkVersion, final Path noticeXmlPath,
      final Path xslPath, final String viewId) throws IOException {
    // XML as input.
    final Source xmlInput = new StreamSource(noticeXmlPath.toFile());

    // HTML as output of the transformation.
    final Path outFolder = Path.of("target", "output-html");
    Files.createDirectories(outFolder);

    final Path htmlPath = outFolder.resolve(viewId + ".html");
    final StreamResult outputTarget = new StreamResult(htmlPath.toFile());
    try (InputStream inputStream = Files.newInputStream(xslPath)) {
      final Source xslSource = new StreamSource(inputStream);

      applyXslTransform(language, sdkVersion, xmlInput, xslSource, outputTarget);

      return htmlPath;
    }
  }

  static void applyXslTransform(final String language, String sdkVersion, final Source xmlInput,
      final Source xslSource, final StreamResult outputTarget) {
    try {
      // XSL for input transformation.
      final TransformerFactory factory = TransformerFactory.newInstance();

      // SECURITY SETUP.
      // https://stackoverflow.com/questions/40649152/how-to-prevent-xxe-attack
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

      // Currently this is what allows to load the labels (i18n).
      factory.setURIResolver(new CustomUriResolver(sdkVersion));
      final Transformer transformer = factory.newTransformer(xslSource);
      // transformer.setURIResolver(uriResolver); Already set by the factory!
      // Parameters.
      transformer.setParameter("language", language);
      transformer.transform(xmlInput, outputTarget);
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
   * @throws InstantiationException 
   */
  public static final Path buildXsl(final String viewId, final String sdkVersion)
      throws IOException, InstantiationException {
    final Path viewPath = getPathToEfxAsStr(viewId, sdkVersion);

    Validate.isTrue(viewPath.toFile().exists(), "No such file: " + viewId);

    try (InputStream viewInputStream = Files.newInputStream(viewPath)) {
      final String translation =
          EfxTranslator.translateTemplate(viewInputStream, new DependencyFactory(), sdkVersion);
      final Path outFolder = Path.of("target", "output-xsl");
      Files.createDirectories(outFolder);

      final String nameByConvention = viewId + ".xsl";
      final Path filePath = outFolder.resolve(nameByConvention);
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
        writer.write(translation);
      }

      return filePath;
    }
  }

  /**
   * @param root The root element of the XML document
   * @return The eforms SDK version as found in the notice xml
   */
  public static Optional<String> getEformsSdkVersion(final Element root) {
    final NodeList nodeList = root.getElementsByTagName("cbc:CustomizationID");
    // We assume that length equals 1 exactly. Anything else is considered
    // empty.
    return nodeList.getLength() == 1
        ? Optional.of(normalizeVersion(nodeList.item(0).getTextContent().strip()))
        : Optional.empty();
  }

  private static String normalizeVersion(final String sdkVersion) {
    return StringUtils.removeStart(sdkVersion, "eforms-sdk-");
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
        return Optional.of(item.getTextContent().strip());
      }
    }
    return Optional.empty();
  }

  /**
   * @param viewId It can correspond to a view id, as long as there is one view id per notice id, or
   *        something else for custom views
   * @param sdkVersion The SDK version to load the path from
   * @throws IOException
   */
  public static Path getPathToEfxAsStr(final String viewId, final String sdkVersion)
      throws IOException {
    SdkDownloader.downloadSdk(sdkVersion, SdkResourceLoader.INSTANCE.getRoot());

    return SdkResourceLoader.INSTANCE.getResourceAsPath(
        SdkConstants.ResourceType.NOTICE_TYPES_VIEW_TEMPLATE, sdkVersion, viewId + ".efx");
  }
}
