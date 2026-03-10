package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import eu.europa.ted.efx.model.DecimalFormat;

/**
 * Tests that verify format-number works correctly in XSL context (as used by the notice viewer).
 *
 * These tests use Saxon (XSLT 3.0), the same processor used by the notice viewer.
 */
class NumberFormattingTest {

  @BeforeAll
  static void setUp() {
    System.setProperty("javax.xml.transform.TransformerFactory",
        "net.sf.saxon.TransformerFactoryImpl");
  }

  /**
   * With XSL_DEFAULT options, no adaptation occurs and format-number works correctly
   * with the standard default decimal format.
   */
  @Test
  void xslDefault_noAdaptation_works() throws Exception {
    String pattern = "#,##0.00";
    String result = evaluateFormatNumber(123456.78, pattern, null, null);
    assertEquals("123,456.78", result);
  }

  /**
   * With EFX_DEFAULT options, the pattern is adapted and the default xsl:decimal-format is
   * overridden to match. This is the current notice viewer approach.
   */
  @Test
  void efxDefault_adaptedPatternWithMatchingDecimalFormat_works() throws Exception {
    String adaptedPattern = DecimalFormat.EFX_DEFAULT.adaptFormatString("#,##0.00");
    assertEquals("# ##0,00", adaptedPattern);

    String result = evaluateFormatNumber(123456.78, adaptedPattern,
        String.valueOf(DecimalFormat.EFX_DEFAULT.getDecimalSeparator()),
        String.valueOf(DecimalFormat.EFX_DEFAULT.getGroupingSeparator()));

    assertEquals("123 456,78", result);
  }

  private String evaluateFormatNumber(double number, String pattern,
      String decimalSeparator, String groupingSeparator) throws Exception {
    String decimalFormatDecl = "";
    if (decimalSeparator != null && groupingSeparator != null) {
      decimalFormatDecl = String.format(
          "<xsl:decimal-format decimal-separator=\"%s\" grouping-separator=\"%s\" />",
          decimalSeparator, groupingSeparator);
    }

    String xsl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<xsl:stylesheet version=\"3.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
        + decimalFormatDecl
        + "<xsl:output method=\"text\"/>"
        + "<xsl:template match=\"/\">"
        + "<xsl:value-of select=\"format-number(" + number + ", '" + pattern + "')\"/>"
        + "</xsl:template>"
        + "</xsl:stylesheet>";

    String xml = "<?xml version=\"1.0\"?><root/>";
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer(
        new StreamSource(new StringReader(xsl)));
    StringWriter writer = new StringWriter();
    transformer.transform(
        new StreamSource(new StringReader(xml)),
        new StreamResult(writer));
    return writer.toString().trim();
  }
}
