package eu.europa.ted.eforms.viewer.util.xml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.resource.SdkResourceLoader;

/**
 * A custom URI resolver for handling paths of translation files. It resolves them based on the SDK
 * version and the SDK root path, allowing for the correct loading of labels.
 */
public final class TranslationUriResolver implements URIResolver {
  private String sdkVersion;
  private Path sdkRoot;

  public TranslationUriResolver(String sdkVersion, Path sdkRoot) {
    this.sdkVersion = sdkVersion;
    this.sdkRoot = sdkRoot;
  }

  public TranslationUriResolver(String sdkVersion, String sdkRootStr) {
    this.sdkVersion = sdkVersion;
    this.sdkRoot = Path.of(sdkRootStr);
  }

  /**
   * This allows to load the labels.
   */
  @Override
  public Source resolve(final String href, final String base) throws TransformerException {
    try (InputStream translationInput = SdkResourceLoader.getResourceAsStream(sdkVersion,
        SdkConstants.SdkResource.TRANSLATIONS, href, sdkRoot)) {
      if (translationInput == null) {
        throw new IllegalArgumentException(
            String.format("Input stream is null for href=%s", href));
      }

      // DOM parser based.
      final DocumentBuilder builder = SafeDocumentBuilder.buildSafeDocumentBuilderAllowDoctype();
      final Document document = Optional.ofNullable(builder.parse(translationInput))
          .orElseThrow(() -> new TransformerException("The document parser returned null."));

      return new DOMSource(document);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new TransformerException(e.toString(), e);
    }
  }
}
