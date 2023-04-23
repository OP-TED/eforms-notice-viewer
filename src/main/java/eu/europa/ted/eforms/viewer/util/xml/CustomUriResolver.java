package eu.europa.ted.eforms.viewer.util.xml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.resource.SdkResourceLoader;

public final class CustomUriResolver implements URIResolver {
  private String sdkVersion;
  private Path sdkRootPath;

  public CustomUriResolver(String sdkVersion, Path sdkRootPath) {
    this.sdkVersion = sdkVersion;
    this.sdkRootPath = sdkRootPath;
  }

  /**
   * Currently this allows to load the labels.
   */
  @Override
  public Source resolve(final String href, final String base) {
    try (InputStream is = SdkResourceLoader.getResourceAsStream(sdkVersion,
        SdkConstants.SdkResource.TRANSLATIONS, href, sdkRootPath)) {
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
