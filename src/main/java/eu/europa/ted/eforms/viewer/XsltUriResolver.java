package eu.europa.ted.eforms.viewer;

import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import eu.europa.ted.eforms.viewer.helpers.ResourceLoader;

class XsltUriResolver implements URIResolver {

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
    // System.out.println("href=" + href);
    // System.out.println("base=" + base);

    final InputStream is = ResourceLoader.getResourceAsStream("eforms-sdk/translations/en.xml");
    if (is == null) {
      throw new RuntimeException(String.format("inputStream is null for href=%s", href));
    }

    final StreamSource streamSource = new StreamSource(is);
    // streamSource.setReader)
    return streamSource;
  }

}
