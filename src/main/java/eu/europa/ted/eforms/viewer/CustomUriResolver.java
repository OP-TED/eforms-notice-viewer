package eu.europa.ted.eforms.viewer;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.viewer.helpers.ResourceLoader;

class CustomUriResolver implements URIResolver {

  @Override
  public Source resolve(final String href, final String base) {
    // TODO lookup in SDK ?
    // IDEALLY WE PASS THE LANGUAGE TO THE XSD AS A PARAMETER.

    // final String newHref;
    // if ("labels.xml".equals(href)) {
    // newHref = "eforms-sdk/translations/en.xml";
    // } else {
    // newHref = href;
    // }

    final InputStream is = ResourceLoader.getResourceAsStream("eforms-sdk/translations/en.xml");
    if (is == null) {
      throw new RuntimeException(
          String.format("inputStream is null for href=%s, base=%s", href, base));
    }

    // DOM parser.
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setValidating(false);
    dbf.setNamespaceAware(true);
    try {
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true); // Security.

      // We do not care about the DTD of language files referenced by the XSL.
      // <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

      final DocumentBuilder builder = dbf.newDocumentBuilder();
      final Document document = builder.parse(is);

      return new DOMSource(document);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new RuntimeException(e.toString(), e);
    }

    // An alternative is to use a StreamSource, this uses less memory.
    // This can fail when it tries to load a dtd (proxy ...).
    // return new StreamSource(is);
  }

}
