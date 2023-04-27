package eu.europa.ted.eforms.viewer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.resource.SdkResourceLoader;
import eu.europa.ted.eforms.viewer.generator.HtmlGenerator;
import eu.europa.ted.eforms.viewer.generator.XslGenerator;
import eu.europa.ted.eforms.viewer.util.xml.TranslationUriResolver;
import eu.europa.ted.efx.interfaces.TranslatorOptions;

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
   * @param sdkRoot Path of the root SDK folder
   * @param forceBuild Forces the re-creation of XSL files
   * @return The path of the generated HTML file
   *
   * @throws IOException If an error occurs during input or output
   * @throws ParserConfigurationException Error related to XML reader configuration
   * @throws SAXException XML parse related errors
   * @throws InstantiationException
   * @throws TransformerException
   */
  public static Path generateHtml(final String language, final Path noticeXmlPath,
      final Optional<String> viewIdOpt, final boolean profileXslt, final Path sdkRoot,
      boolean forceBuild, TranslatorOptions translatorOptions)
      throws IOException, SAXException, ParserConfigurationException, TransformerException {
    Validate.notNull(noticeXmlPath, "Invalid path to notice: " + noticeXmlPath);
    Validate.isTrue(Files.isRegularFile(noticeXmlPath), "No such file: " + noticeXmlPath);

    logger.debug("noticeXmlPath={}", noticeXmlPath);

    final NoticeDocument notice = new NoticeDocument(noticeXmlPath);
    final String eformsSdkVersion = notice.getEformsSdkVersion();
    Validate.notBlank(eformsSdkVersion, "Undefined SDK version");

    final String viewId = viewIdOpt.isPresent() ? viewIdOpt.get() : notice.getNoticeSubType();

    logger.debug("View ID: {}, SDK version: {}", viewId, eformsSdkVersion);

    final Path efxPath = getEfxPath(eformsSdkVersion, viewId, sdkRoot);

    try (InputStream efxInput = Files.newInputStream(efxPath)) {
      final Path xslPath =
          XslGenerator.Builder
              .create(new DependencyFactory(sdkRoot))
              .withTranslatorOptions(translatorOptions)
              .build()
              .generate(eformsSdkVersion, viewId, efxInput, forceBuild);

      final Path htmlPath = HtmlGenerator.Builder
          .create()
          .withProfileXslt(profileXslt)
          .withUriResolver(new TranslationUriResolver(eformsSdkVersion, sdkRoot))
          .build()
          .generateFile(language, viewId, noticeXmlPath, xslPath);

      // Ensure the HTML can be parsed.
      Jsoup.parse(htmlPath.toFile(), NoticeViewerConstants.DEFAULT_CHARSET.toString());

      return htmlPath;
    }
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
      final boolean profileXslt, final Path sdkRoot)
      throws IOException, SAXException, ParserConfigurationException {
    Validate.notBlank(noticeXmlContent, "Invalid notice content: " + noticeXmlContent);

    logger.debug("noticeXmlContent={} ...", StringUtils.left(noticeXmlContent, 50));

    try (
        final ByteArrayInputStream noticeXmlInputStream =
            new ByteArrayInputStream(noticeXmlContent.trim().getBytes(charset));
        final ByteArrayInputStream xslInputStream =
            new ByteArrayInputStream(xsl.getBytes(charset));) {

      return generateHtml(language, noticeXmlInputStream, xslInputStream, charset, viewIdOpt,
          profileXslt, sdkRoot);
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
      final Path sdkRoot) throws IOException, ParserConfigurationException, SAXException {
    Validate.notNull(noticeXmlContent, "Notice XML content is null");
    Validate.notNull(xslIs, "XSL input is null");

    try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
      noticeXmlContent.transferTo(baos);
      final byte[] xmlContentBytes = baos.toByteArray();

      try (final InputStream noticeXmlIsClone1 = new ByteArrayInputStream(xmlContentBytes);
          final InputStream noticeXmlIsClone2 = new ByteArrayInputStream(xmlContentBytes);) {
        final NoticeDocument notice = new NoticeDocument(noticeXmlIsClone1);
        final String eformsSdkVersion = notice.getEformsSdkVersion();
        final String viewId = viewIdOpt.isPresent() ? viewIdOpt.get() : notice.getNoticeSubType();

        logger.debug("viewId={}, eformsSdkVersion={}", viewId, eformsSdkVersion);

        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
          return HtmlGenerator.Builder
              .create()
              .withCharset(charset)
              .withProfileXslt(profileXslt)
              .withUriResolver(new TranslationUriResolver(eformsSdkVersion, sdkRoot))
              .build()
              .generateString(language, viewId, new StreamSource(noticeXmlIsClone2),
                  new StreamSource(xslIs));
        }
      }
    }
  }

  /**
   * @param viewId It can correspond to a view id, as long as there is one view id per notice id, or
   *        something else for custom views
   * @throws FileNotFoundException
   */
  public static Path getEfxPath(final String sdkVersion, final String viewId, final Path sdkRoot)
      throws FileNotFoundException {
    Path efxPath = SdkResourceLoader.getResourceAsPath(sdkVersion,
        SdkConstants.SdkResource.VIEW_TEMPLATES, MessageFormat.format("{0}.efx", viewId), sdkRoot);

    Validate.notNull(efxPath,
        "Failed to create EFX path for view ID [{}], SDK version [{}] and SDK root directory [{}]",
        viewId, sdkVersion, sdkRoot);

    if (!Files.isRegularFile(efxPath)) {
      throw new FileNotFoundException(efxPath.toString());
    }

    logger.debug("EFX path for view ID {} and SDK {}: {}", viewId, sdkVersion, efxPath);

    return efxPath;
  }
}
