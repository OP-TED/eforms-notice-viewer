package eu.europa.ted.eforms.viewer.generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.viewer.NoticeViewerConstants;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.trace.TimingTraceListener;

public class HtmlGenerator {
  private static final Logger logger = LoggerFactory.getLogger(HtmlGenerator.class);

  private static final String MSG_INVALID_XML_CONTENTS = "Invalid XML contents";
  private static final String MSG_INVALID_XSL_CONTENTS = "Invalid XSL contents";
  private static final String MSG_UNDEFINED_LANGUAGE = "Undefined language";
  private static final String MSG_UNDEFINED_OUTPUT = "Undefined output";
  private static final String MSG_UNDEFINED_VIEW_ID = "Undefined view ID";

  private final Charset charset;
  private final boolean profileXslt;
  private final URIResolver uriResolver;

  public HtmlGenerator(final Charset charset, final URIResolver uriResolver,
      final boolean profileXslt) {
    this.charset = ObjectUtils.defaultIfNull(charset, NoticeViewerConstants.DEFAULT_CHARSET);
    this.profileXslt = profileXslt;
    this.uriResolver = uriResolver;
  }

  private HtmlGenerator(final Builder builder) {
    this(builder.charset, builder.uriResolver, builder.profileXslt);
  }

  /**
   * Generates HTML and writes the result to a file.
   *
   * @param language The language to use as a two letter code
   * @param viewId The view ID corresponding to the XSL template.
   * @param xmlContents The contents of the notice XML
   * @param xslContents The contents of the XSL template
   * @param outputFile The target output file
   * @return The path of the output file
   * @throws TransformerException when the XSL transformation fails
   * @throws IOException when the XML/XSL contents cannot be loaded or the output file cannot be
   *         written to
   */
  public Path generateFile(final String language, final String viewId, final String xmlContents,
      final String xslContents, final Path outputFile) throws IOException, TransformerException {
    Validate.notBlank(xmlContents, MSG_INVALID_XML_CONTENTS);
    Validate.notBlank(xslContents, MSG_INVALID_XSL_CONTENTS);

    final Path htmlPath =
        ObjectUtils.defaultIfNull(outputFile, NoticeViewerConstants.OUTPUT_FOLDER_HTML
            .resolve(MessageFormat.format("{0}-{1}.html", viewId, language)));

    logger.debug("Writing HTML for view ID [{}] to file [{}]", viewId, htmlPath);

    Files.createDirectories(htmlPath.getParent());
    final StreamResult output = new StreamResult(htmlPath.toFile());

    applyXslTransformation(language, viewId, xmlContents, xslContents, output);

    logger.info("Wrote HTML for view ID [{}] to file [{}]", viewId, htmlPath);

    return htmlPath;
  }

  /**
   * Generates HTML and returns the contents as a string.
   *
   * @param language The language to use as a two letter code
   * @param viewId The view ID corresponding to the XSL template.
   * @param xmlContents The contents of the notice XML
   * @param xslContents The contents of the XSL template
   * @return
   * @throws TransformerException when the XSL transformation fails
   * @throws IOException when the XML/XSL contents cannot be loaded
   */
  public String generateString(final String language, final String viewId, final String xmlContents,
      final String xslContents) throws TransformerException, IOException {
    Validate.notBlank(language, MSG_UNDEFINED_LANGUAGE);
    Validate.notBlank(xmlContents, MSG_INVALID_XML_CONTENTS);
    Validate.notBlank(xslContents, MSG_INVALID_XSL_CONTENTS);

    try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      final StreamResult output = new StreamResult(outputStream);

      applyXslTransformation(language, viewId, xmlContents, xslContents, output);

      final String htmlText = outputStream.toString(charset);

      // Ensure the HTML can be parsed.
      Jsoup.parse(htmlText);

      return htmlText;
    }
  }

  /**
   * Applies an XSL transformation on a notice XML using a XSL template, both passed as strings.
   * <p>
   * The output is written to the provided {@link StreamResult} instance.
   *
   * @param language The language to use as a two letter code
   * @param viewId The view ID corresponding to the XSL template.
   * @param xmlContents The contents of the notice XML
   * @param xslContents The contents of the XSL template
   * @param output Output for the transformation, as a {@link StreamResult} instance
   * @throws TransformerException when the XSL transformation fails
   * @throws IOException when the XML/XSL contents cannot be loaded
   */
  private void applyXslTransformation(final String language, final String viewId,
      final String xmlContents, final String xslContents, final StreamResult output)
      throws TransformerException, IOException {
    Validate.notBlank(language, MSG_UNDEFINED_LANGUAGE);
    Validate.notBlank(viewId, MSG_UNDEFINED_VIEW_ID);
    Validate.notBlank(xmlContents, MSG_INVALID_XML_CONTENTS);
    Validate.notBlank(xslContents, MSG_INVALID_XSL_CONTENTS);
    Validate.notNull(output, MSG_UNDEFINED_OUTPUT);

    try (InputStream xmlInput = IOUtils.toInputStream(xmlContents, charset)) {
      final Transformer transformer = getTransformer(language, viewId, xslContents);
      final Source xmlSource = new StreamSource(xmlInput);

      logger.info("Applying XSL transformation for language [{}] and view ID [{}]", language,
          viewId);
      logger.trace("XML input:\n{}", xmlContents);
      logger.trace("XSL input:\n{}", xslContents);

      transformer.transform(xmlSource, output);

      logger.info("XSL transformation succeeded for language [{}] and view ID [{}]", language,
          viewId);
    }
  }

  /**
   * Creates and configures a {@link TransformerFactory}
   *
   * @return A configured {@link TransformerFactory} instance
   * @throws TransformerConfigurationException when the configuration fails
   */
  private TransformerFactory getTransformerFactory() throws TransformerConfigurationException {
    logger.debug("Creating XSL transformer factory");

    // XSL for input transformation.
    final TransformerFactory factory = TransformerFactory.newInstance();

    // SECURITY SETUP.
    // https://stackoverflow.com/questions/40649152/how-to-prevent-xxe-attack
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

    if (uriResolver != null) {
      factory.setURIResolver(uriResolver);
    }

    logger.debug("Successfully created XSL transformer factory");

    return factory;
  }

  /**
   * Creates and configures a {@link Transformer}
   *
   * @param language The language to use as a two letter code
   * @param viewId The view ID corresponding to the XSL template.
   * @param xslContents The contents of the XSL template
   * @return A configured {@link Transformer} instance
   * @throws TransformerConfigurationException when the configuration fails
   * @throws IOException when the XSL contents cannot be loaded
   */
  private Transformer getTransformer(final String language, final String viewId,
      final String xslContents) throws TransformerConfigurationException, IOException {
    Validate.notBlank(language, MSG_UNDEFINED_LANGUAGE);
    Validate.notBlank(viewId, MSG_UNDEFINED_VIEW_ID);
    Validate.notBlank(xslContents, MSG_INVALID_XSL_CONTENTS);

    final TransformerFactory factory = getTransformerFactory();

    if (profileXslt) {
      final Path xsltProfilePath = NoticeViewerConstants.OUTPUT_FOLDER_PROFILER
          .resolve(MessageFormat.format("{0}-{1}-xslt_profile.html", viewId, language));
      Files.createDirectories(xsltProfilePath.getParent());

      logger.info("XSLT profiling is enabled. The result can be found at: {}", xsltProfilePath);

      factory.setAttribute(FeatureKeys.TRACE_LISTENER_CLASS, TimingTraceListener.class.getName());
      factory.setAttribute(FeatureKeys.TRACE_LISTENER_OUTPUT_FILE, xsltProfilePath.toString());
    }

    logger.debug("Creating XSL transformer for language [{}] and view ID [{}]", language, viewId);

    try (InputStream xslInput = IOUtils.toInputStream(xslContents, charset)) {
      Validate.notNull(xslInput, "XSL input stream is null");

      final Source xslSource = new StreamSource(xslInput);
      final Transformer transformer = factory.newTransformer(xslSource);
      Validate.notNull(transformer, "No transformer was created");

      transformer.setOutputProperty(OutputKeys.ENCODING, charset.name());
      transformer.setParameter("language", language);

      logger.debug("Successfully created XSL transformer for language [{}] and view ID [{}]",
          language, viewId);

      return transformer;
    }
  }

  /**
   * Builder class for {@link HtmlGenerator} instances
   */
  public static final class Builder {
    // required parameters

    // optional parameters
    private Charset charset;
    private boolean profileXslt;
    private URIResolver uriResolver;

    public static Builder create() {
      return new Builder();
    }

    /**
     * @param charset The character set to be used for the HTML output and for reading the XSL
     *        template
     * @return A {@link Builder} instance
     */
    public Builder withCharset(final Charset charset) {
      this.charset = charset;
      return this;
    }

    /**
     * @param profileXslt If true, Enables XSLT profiling
     * @return A {@link Builder} instance
     */
    public Builder withProfileXslt(final boolean profileXslt) {
      this.profileXslt = profileXslt;
      return this;
    }

    /**
     * @param uriResolver The URI resolver to be used during the XSL transformation
     * @return A {@link Builder} instance
     */
    public Builder withUriResolver(final URIResolver uriResolver) {
      this.uriResolver = uriResolver;
      return this;
    }

    /**
     * @return A configured {@link HtmlGenerator} instance
     */
    public HtmlGenerator build() {
      return new HtmlGenerator(this);
    }
  }
}
