package eu.europa.ted.eforms.viewer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
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
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.viewer.generator.XslGenerator;
import eu.europa.ted.eforms.viewer.util.xml.CustomUriResolver;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.trace.TimingTraceListener;

public class NoticeViewer {
  private static final Logger logger = LoggerFactory.getLogger(NoticeViewer.class);

  private NoticeViewer() {}

  /**
   * @param language The language as a two letter code
   * @param noticeXmlFilename The notice xml filename but without the xml extension
   * @param viewIdOpt An optional SDK view id to use, this can be used to enforce a custom view like
   *        notice summary. It could fail if this custom view is not compatible with the notice sub
   *        type
   * @param profileXslt If set to true, XSLT profiling will be enabled
   * @param sdkRootPath Path of the root SDK folder
   * @param forceBuild Forces the re-creation of XSL files
   * @return The path of the generated HTML file
   *
   * @throws IOException If an error occurs during input or output
   * @throws ParserConfigurationException Error related to XML reader configuration
   * @throws SAXException XML parse related errors
   * @throws InstantiationException
   */
  public static Path generateHtml(final String language, final Path noticeXmlPath,
      final Optional<String> viewIdOpt, final boolean profileXslt, final Path sdkRootPath,
      boolean forceBuild)
      throws IOException, SAXException, ParserConfigurationException, InstantiationException {
    Validate.notNull(noticeXmlPath, "Invalid path to notice: " + noticeXmlPath);
    Validate.isTrue(Files.isRegularFile(noticeXmlPath), "No such file: " + noticeXmlPath);

    logger.debug("noticeXmlPath={}", noticeXmlPath);

    final NoticeDocument notice = new NoticeDocument(noticeXmlPath);

    final String eformsSdkVersion = notice.getEformsSdkVersion();

    // Build XSL from EFX.
    final String viewId = viewIdOpt.isPresent() ? viewIdOpt.get() : notice.getNoticeSubType();

    logger.debug("viewId={}, eformsSdkVersion={}", viewId, eformsSdkVersion);

    final Path xslPath =
        new XslGenerator(eformsSdkVersion, sdkRootPath).generate(viewId, forceBuild);
    final Path htmlPath = applyXslTransform(language, eformsSdkVersion, noticeXmlPath, xslPath,
        viewId, profileXslt, sdkRootPath);

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
      final String xsl, final Charset charset, final Optional<String> viewIdOpt,
      final boolean profileXslt, final Path sdkRootPath)
      throws IOException, SAXException, ParserConfigurationException {
    logger.debug("noticeXmlContent={} ...", StringUtils.left(noticeXmlContent, 50));

    Validate.notNull(noticeXmlContent, "Invalid notice content: " + noticeXmlContent);

    try (
        final ByteArrayInputStream noticeXmlInputStream =
            new ByteArrayInputStream(noticeXmlContent.trim().getBytes(charset));
        final ByteArrayInputStream xslInputStream =
            new ByteArrayInputStream(xsl.getBytes(charset));) {

      return generateHtml(language, noticeXmlInputStream, xslInputStream, charset, viewIdOpt,
          profileXslt, sdkRootPath);
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
      final Charset charset, final Optional<String> viewIdOpt, final boolean profileXslt,
      final Path sdkRootPath) throws IOException, ParserConfigurationException, SAXException {
    try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
      noticeXmlContent.transferTo(baos);

      try (final InputStream noticeXmlIsClone1 = new ByteArrayInputStream(baos.toByteArray());
          final InputStream noticeXmlIsClone2 = new ByteArrayInputStream(baos.toByteArray());) {
        final NoticeDocument notice = new NoticeDocument(noticeXmlIsClone1);

        final String eformsSdkVersion = notice.getEformsSdkVersion();

        // Build XSL from EFX.
        final String viewId = viewIdOpt.isPresent() ? viewIdOpt.get() : notice.getNoticeSubType();

        logger.debug("viewId={}, eformsSdkVersion={}", viewId, eformsSdkVersion);

        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
          final StreamResult htmlResult = new StreamResult(outputStream);

          applyXslTransform(language, eformsSdkVersion, viewId, new StreamSource(noticeXmlIsClone2),
              new StreamSource(xslIs), htmlResult, profileXslt, sdkRootPath);

          // Ensure the HTML can be parsed.
          final String htmlText = outputStream.toString(charset);
          Jsoup.parse(htmlText);

          return htmlText;
        }
      }
    }
  }

  static TransformerFactory getTransformerFactory(final String sdkVersion, final String viewId,
      final boolean profileXslt, final Path sdkRootPath) throws TransformerConfigurationException {
    logger.debug("Creating XSL transformer factory for SDK version [{}]", sdkVersion);

    // XSL for input transformation.
    final TransformerFactory factory = TransformerFactory.newInstance();

    // SECURITY SETUP.
    // https://stackoverflow.com/questions/40649152/how-to-prevent-xxe-attack
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

    // Currently this is what allows to load the labels (i18n).
    factory.setURIResolver(new CustomUriResolver(sdkVersion, sdkRootPath));

    if (profileXslt) {
      final Path xsltProfilePath =
          NoticeViewerConstants.OUTPUT_FOLDER_HTML.resolve(viewId + "-xslt_profile.html");
      logger.info("XSLT profiling is enabled. The result can be found at: {}", xsltProfilePath);

      factory.setAttribute(FeatureKeys.TRACE_LISTENER_CLASS, TimingTraceListener.class.getName());
      factory.setAttribute(FeatureKeys.TRACE_LISTENER_OUTPUT_FILE, xsltProfilePath.toString());
    }

    logger.debug("Successfully created XSL transformer factory for SDK version [{}]", sdkVersion);

    return factory;
  }

  static Transformer getTransformer(final String language, final String sdkVersion,
      final String viewId, final Source xslSource, final boolean profileXslt,
      final Path sdkRootPath) throws TransformerConfigurationException {
    final TransformerFactory factory =
        getTransformerFactory(sdkVersion, viewId, profileXslt, sdkRootPath);

    logger.debug("Creating XSL transformer for SDK version [{}], language [{}] and view ID [{}]",
        sdkVersion, language, viewId);

    final Transformer transformer = factory.newTransformer(xslSource);
    transformer.setParameter("language", language);

    logger.debug(
        "Successfully created XSL transformer for SDK version [{}], language [{}] and view ID [{}]",
        sdkVersion, language, viewId);

    return transformer;
  }

  static Path applyXslTransform(final String language, String sdkVersion, final Path noticeXmlPath,
      final Path xslPath, final String viewId, final boolean profileXslt, final Path sdkRootPath)
      throws IOException {
    // XML as input.
    final Source xmlInput = new StreamSource(noticeXmlPath.toFile());

    // HTML as output of the transformation.
    Files.createDirectories(NoticeViewerConstants.OUTPUT_FOLDER_HTML);

    final Path htmlPath = NoticeViewerConstants.OUTPUT_FOLDER_HTML.resolve(viewId + ".html");
    final StreamResult outputTarget = new StreamResult(htmlPath.toFile());
    try (InputStream inputStream = Files.newInputStream(xslPath)) {
      final Source xslSource = new StreamSource(inputStream);

      applyXslTransform(language, sdkVersion, viewId, xmlInput, xslSource, outputTarget,
          profileXslt, sdkRootPath);

      return htmlPath;
    }
  }

  static void applyXslTransform(final String language, String sdkVersion, String viewId,
      final Source xmlInput, final Source xslSource, final StreamResult outputTarget,
      final boolean profileXslt, final Path sdkRootPath) {
    try {
      final Transformer transformer =
          getTransformer(language, sdkVersion, viewId, xslSource, profileXslt, sdkRootPath);

      logger.info(
          "Applying XSL transformation for language [{}] and SDK version [{}] with: XML input={}",
          language, sdkVersion, xmlInput.getSystemId());

      transformer.transform(xmlInput, outputTarget);

      logger.debug(
          "Finished applying XSL transformation for language [{}] and SDK version [{}] with: XML input={}, XSL Source={}",
          language, sdkVersion, xmlInput.getSystemId(), xslSource.getSystemId());
    } catch (TransformerFactoryConfigurationError | TransformerException e) {
      throw new RuntimeException(e.toString(), e);
    }
  }
}
