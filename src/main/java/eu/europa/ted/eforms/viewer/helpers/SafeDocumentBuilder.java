package eu.europa.ted.eforms.viewer.helpers;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeDocumentBuilder {
  private static final Logger logger = LoggerFactory.getLogger(SafeDocumentBuilder.class);

  private SafeDocumentBuilder() {
    throw new AssertionError("Utility class.");
  }

  public static DocumentBuilder buildSafeDocumentBuilderAllowDoctype()
      throws ParserConfigurationException {
    return buildSafeDocumentBuilder(false);
  }

  public static DocumentBuilder buildSafeDocumentBuilderStrict()
      throws ParserConfigurationException {
    return buildSafeDocumentBuilder(true);
  }

  private static DocumentBuilder buildSafeDocumentBuilder(final boolean disallowDoctypeDecl)
      throws ParserConfigurationException {
    // https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#java
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
    String FEATURE = null;
    try {
      // This is the PRIMARY defence. If DTDs (doctypes) are disallowed, almost
      // all
      // XML entity attacks are prevented
      // Xerces 2 only -
      // http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
      FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
      dbf.setFeature(FEATURE, disallowDoctypeDecl);
      // If you can't completely disable DTDs, then at least do the following:
      // Xerces 1 -
      // http://xerces.apache.org/xerces-j/features.html#external-general-entities
      // Xerces 2 -
      // http://xerces.apache.org/xerces2-j/features.html#external-general-entities
      // JDK7+ - http://xml.org/sax/features/external-general-entities
      // This feature has to be used together with the following one, otherwise
      // it will not protect
      // you from XXE for sure
      FEATURE = "http://xml.org/sax/features/external-general-entities";
      dbf.setFeature(FEATURE, false);
      // Xerces 1 -
      // http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
      // Xerces 2 -
      // http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
      // JDK7+ - http://xml.org/sax/features/external-parameter-entities
      // This feature has to be used together with the previous one, otherwise
      // it will not protect
      // you from XXE for sure
      FEATURE = "http://xml.org/sax/features/external-parameter-entities";
      dbf.setFeature(FEATURE, false);
      // Disable external DTDs as well
      FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
      dbf.setFeature(FEATURE, false);
      // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD,
      // and Entity Attacks"
      dbf.setXIncludeAware(false);
      dbf.setExpandEntityReferences(false);
      // And, per Timothy Morgan: "If for some reason support for inline
      // DOCTYPEs are a requirement,
      // then
      // ensure the entity settings are disabled (as shown above) and beware
      // that SSRF attacks
      // (http://cwe.mitre.org/data/definitions/918.html) and denial
      // of service attacks (such as billion laughs or decompression bombs via
      // "jar:") are a risk."
      //
      // CUSTOM ADDITIONAL SETUP.
      //
      // https://stackoverflow.com/questions/40649152/how-to-prevent-xxe-attack
      dbf.setValidating(false);
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      // We do not care about the DTD of language files referenced by the XSL.
      // <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
      // This is already set above.
      // dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
      // false);
      return dbf.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      // This should catch a failed setFeature feature
      logger.info("ParserConfigurationException was thrown. The feature '" + FEATURE
          + "' is probably not supported by your XML processor.");
      throw e;
    }
  }
}
