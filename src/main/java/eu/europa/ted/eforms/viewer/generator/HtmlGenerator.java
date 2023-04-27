package eu.europa.ted.eforms.viewer.generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.viewer.NoticeViewerConstants;
import eu.europa.ted.eforms.viewer.util.xml.TranslationUriResolver;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.trace.TimingTraceListener;

public class HtmlGenerator {
  private static final Logger logger = LoggerFactory.getLogger(HtmlGenerator.class);

  private final String sdkVersion;
  private final Path sdkRoot;
  private final Charset charset;
  private final boolean profileXslt;

  public HtmlGenerator(final String sdkVersion, final Path sdkRoot, Charset charset,
      boolean profileXslt) {
    Validate.notBlank(sdkVersion, "Undefined SDK version");

    Validate.notNull(sdkRoot, "Undefined SDK root");
    Validate.isTrue(Files.isDirectory(sdkRoot),
        MessageFormat.format("SDK root directory not found: {0}", sdkRoot));

    this.sdkVersion = sdkVersion;
    this.sdkRoot = sdkRoot;
    this.charset = ObjectUtils.defaultIfNull(charset, NoticeViewerConstants.DEFAULT_CHARSET);
    this.profileXslt = profileXslt;
  }

  private HtmlGenerator(Builder builder) {
    this(builder.sdkVersion, builder.sdkRoot, builder.charset, builder.profileXslt);
  }

  public Path generateFile(final String language, final String viewId, final Path noticeXmlPath,
      final Path xslPath) throws IOException, TransformerException {
    Validate.notNull(noticeXmlPath, "Undefined notice XML path");
    Validate.isTrue(Files.isRegularFile(noticeXmlPath),
        MessageFormat.format("Notice XML [{0}] does not exist", noticeXmlPath));

    Validate.notNull(xslPath, "Undefined XSL path");
    Validate.isTrue(Files.isRegularFile(xslPath),
        MessageFormat.format("XSL file [{0}] does not exist", xslPath));

    try (InputStream xslInput = Files.newInputStream(xslPath)) {
      final Source xmlSource = new StreamSource(noticeXmlPath.toFile());
      final Source xslSource = new StreamSource(xslInput);

      // HTML as output of the transformation.
      Files.createDirectories(NoticeViewerConstants.OUTPUT_FOLDER_HTML);
      final Path htmlPath = NoticeViewerConstants.OUTPUT_FOLDER_HTML
          .resolve(MessageFormat.format("{0}-{1}.html", viewId, language));
      final StreamResult outputTarget = new StreamResult(htmlPath.toFile());

      applyXslTransformation(language, viewId, xmlSource, xslSource, outputTarget);

      return htmlPath;
    }
  }

  public String generateString(final String language, final String viewId,
      final InputStream xmlInput, final InputStream xslInput) throws IOException {
    Validate.notNull(xmlInput, "Undefined XML input");
    Validate.notNull(xslInput, "Undefined XSL input");

    final Source xmlSource = new StreamSource(xmlInput);
    final Source xslSource = new StreamSource(xslInput);

    return generateString(language, viewId, xmlSource, xslSource);
  }

  public String generateString(final String language, String viewId, final Source xmlSource,
      final Source xslSource) throws IOException {
    Validate.notNull(xmlSource, "Undefined XML source");
    Validate.notNull(xslSource, "Undefined XSL source");

    try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      final StreamResult output = new StreamResult(outputStream);
      applyXslTransformation(language, viewId, xmlSource, xslSource, output);

      final String htmlText = outputStream.toString(charset);

      // Ensure the HTML can be parsed.
      Jsoup.parse(htmlText);

      return htmlText;
    } catch (TransformerFactoryConfigurationError | TransformerException e) {
      throw new RuntimeException(e.toString(), e);
    }
  }

  private TransformerFactory getTransformerFactory() throws TransformerConfigurationException {
    logger.debug("Creating XSL transformer factory for SDK version [{}]", sdkVersion);

    // XSL for input transformation.
    final TransformerFactory factory = TransformerFactory.newInstance();

    // SECURITY SETUP.
    // https://stackoverflow.com/questions/40649152/how-to-prevent-xxe-attack
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

    // Currently this is what allows to load the labels (i18n).
    factory.setURIResolver(new TranslationUriResolver(sdkVersion, sdkRoot));

    logger.debug("Successfully created XSL transformer factory for SDK version [{}]", sdkVersion);

    return factory;
  }

  private Transformer getTransformer(final String language, final String viewId,
      final Source xslSource) throws TransformerConfigurationException, IOException {
    final TransformerFactory factory = getTransformerFactory();

    if (profileXslt) {
      Files.createDirectories(NoticeViewerConstants.OUTPUT_FOLDER_HTML);

      final Path xsltProfilePath =
          NoticeViewerConstants.OUTPUT_FOLDER_HTML
              .resolve(MessageFormat.format("{0}-{1}-xslt_profile.html", viewId, language));
      logger.info("XSLT profiling is enabled. The result can be found at: {}", xsltProfilePath);

      factory.setAttribute(FeatureKeys.TRACE_LISTENER_CLASS, TimingTraceListener.class.getName());
      factory.setAttribute(FeatureKeys.TRACE_LISTENER_OUTPUT_FILE, xsltProfilePath.toString());
    }

    logger.debug("Creating XSL transformer for SDK version [{}], language [{}] and view ID [{}]",
        sdkVersion, language, viewId);

    final Transformer transformer = factory.newTransformer(xslSource);
    if (StringUtils.isNotBlank(language)) {
      transformer.setParameter("language", language);
    }

    logger.debug(
        "Successfully created XSL transformer for SDK version [{}], language [{}] and view ID [{}]",
        sdkVersion, language, viewId);

    return transformer;
  }

  private void applyXslTransformation(final String language, final String viewId,
      final Source xmlSource, final Source xslSource, final StreamResult output)
      throws IOException, TransformerException {
    Validate.notBlank(language, "Undefined language");
    Validate.notBlank(viewId, "Undefined viewId");
    Validate.notNull(xmlSource, "Undefined XML source");
    Validate.notNull(xslSource, "Undefined XSL source");
    Validate.notNull(output, "Undefined output");

    final Transformer transformer = getTransformer(language, viewId, xslSource);

    logger.info(
        "Applying XSL transformation for language [{}] and SDK version [{}] with: XML input={}",
        language, sdkVersion, xmlSource.getSystemId());

    transformer.transform(xmlSource, output);

    logger.debug(
        "Finished applying XSL transformation for language [{}] and SDK version [{}] with: XML source={}, XSL source={}",
        language, sdkVersion, xmlSource.getSystemId(), xslSource.getSystemId());
  }

  public static final class Builder {
    // required parameters
    private final String sdkVersion;
    private final Path sdkRoot;

    // optional parameters
    private Charset charset;
    private boolean profileXslt;

    public Builder(final String sdkVersion, final Path sdkRoot) {
      this.sdkVersion = sdkVersion;
      this.sdkRoot = sdkRoot;
    }

    public static Builder create(final String sdkVersion, final Path sdkRoot) {
      return new Builder(sdkVersion, sdkRoot);
    }

    public Builder withCharset(final Charset charset) {
      this.charset = charset;
      return this;
    }

    public Builder withProfileXslt(final boolean profileXslt) {
      this.profileXslt = profileXslt;
      return this;
    }

    public HtmlGenerator build() {
      return new HtmlGenerator(this);
    }
  }
}
