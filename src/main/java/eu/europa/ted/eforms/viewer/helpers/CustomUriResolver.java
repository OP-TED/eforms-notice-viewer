package eu.europa.ted.eforms.viewer.helpers;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.resource.ResourceLoader;

public final class CustomUriResolver implements URIResolver {
  private String sdkVersion;

  public CustomUriResolver(String sdkVersion) {
    this.sdkVersion = sdkVersion;
  }

  /**
   * Currently this allows to load the labels.
   */
  @Override
  public Source resolve(final String href, final String base) {
    try (InputStream is = ResourceLoader.INSTANCE
        .getResourceAsStream(SdkConstants.SdkResource.TRANSLATIONS, sdkVersion, href)) {
      if (is == null) {
        throw new RuntimeException(
            String.format("inputStream is null for href=%s, base=%s", href, base));
      }
      // DOM parser based.
      final DocumentBuilder builder = SafeDocumentBuilder.buildSafeDocumentBuilderAllowDoctype();
      final Document document = builder.parse(is);
      return new DOMSource(document);
    } catch (SAXException | IOException | ParserConfigurationException e) {
      throw new RuntimeException(e.toString(), e);
    }
  }
}
