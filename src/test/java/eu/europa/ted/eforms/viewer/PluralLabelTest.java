package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;

/**
 * Tests the selection of plural label forms (TEDEFO-5207): the CLDR plural rules of
 * efx:plural-label-suffix, and the label lookup with its fallback in the label templates.
 *
 * The tests render the bundled Freemarker templates and run the resulting XSLT with Saxon, the
 * same processor used by the notice viewer.
 */
class PluralLabelTest {

  private static final String WEEK = "code|name|duration-unit.WEEK";

  private static Configuration freemarker;

  @BeforeAll
  static void setUp() {
    System.setProperty("javax.xml.transform.TransformerFactory",
        "net.sf.saxon.TransformerFactoryImpl");
    freemarker = new Configuration(Configuration.VERSION_2_3_31);
    freemarker.setTemplateLoader(new ClassTemplateLoader(PluralLabelTest.class, "/templates"));
  }

  @ParameterizedTest(name = "{0}, {1} -> ''{2}''")
  @CsvSource({
      // English and the other languages with one and other only
      "en, 1, ''", "en, 0, .plural", "en, 2, .plural", "en, 1.5, .plural", "en, -1, ''",
      "de, 1, ''", "de, 5, .plural", "hu, 2, .plural", "EN, 1, ''",
      // French: 0 and 1 are singular; exact millions are many
      "fr, 0, ''", "fr, 1, ''", "fr, 1.5, ''", "fr, 2, .plural", "fr, 1000000, .many",
      // Spanish, Italian, Portuguese (pt_PT)
      "es, 1, ''", "es, 2000000, .many", "it, 1, ''", "pt, 0, .plural", "pt, 1, ''",
      // Danish: 0.5 and 1.5 are singular
      "da, 1, ''", "da, 0.5, ''", "da, 2, .plural",
      // Polish: the last digits decide
      "pl, 1, ''", "pl, 2, .few", "pl, 5, .many", "pl, 12, .many", "pl, 22, .few", "pl, 1.5, .plural",
      "PL, 22, .few",
      // Czech and Slovak
      "cs, 1, ''", "cs, 3, .few", "cs, 5, .plural", "cs, 1.5, .many", "sk, 4, .few",
      // Croatian
      "hr, 21, ''", "hr, 12, .plural", "hr, 23, .few", "hr, 5, .plural",
      // Latvian
      "lv, 0, .zero", "lv, 11, .zero", "lv, 21, ''", "lv, 2, .plural",
      // Lithuanian
      "lt, 1, ''", "lt, 11, .plural", "lt, 21, ''", "lt, 2, .few", "lt, 1.5, .many",
      // Romanian
      "ro, 1, ''", "ro, 2, .few", "ro, 19, .few", "ro, 20, .plural", "ro, 101, .few",
      // Slovenian
      "sl, 1, ''", "sl, 2, .two", "sl, 3, .few", "sl, 5, .plural", "sl, 101, ''", "sl, 102, .two",
      // Maltese
      "mt, 0, .few", "mt, 1, ''", "mt, 2, .two", "mt, 11, .many", "mt, 20, .plural",
      // Irish
      "ga, 1, ''", "ga, 2, .two", "ga, 5, .few", "ga, 8, .many", "ga, 11, .plural",
  })
  void pluralLabelSuffix(String language, String quantity, String expectedSuffix)
      throws Exception {
    final String suffix = transform(
        stylesheet("<xsl:value-of select=\"efx:plural-label-suffix(xs:decimal('" + quantity
            + "'))\"/>"),
        "<root/>", language);
    assertEquals(expectedSuffix, suffix);
  }

  @Test
  void pluralLabelSuffix_emptyQuantity_selectsTheSingular() throws Exception {
    final String suffix = transform(
        stylesheet("<xsl:value-of select=\"efx:plural-label-suffix(())\"/>"), "<root/>", "pl");
    assertEquals("", suffix);
  }

  /**
   * A quantity taken from a field is an xs:double. It must be converted where the function is
   * called, otherwise the stylesheet does not compile.
   */
  @Test
  void labelFromKey_quantityFromField_selectsTheSpecificForm() throws Exception {
    assertEquals("tygodnie", labelFromKey("html", "22", entry(WEEK, "tydzień")
        + entry(WEEK + ".few", "tygodnie") + entry(WEEK + ".many", "tygodni")));
    assertEquals("tygodni", labelFromKey("json", "5", entry(WEEK, "tydzień")
        + entry(WEEK + ".few", "tygodnie") + entry(WEEK + ".many", "tygodni")));
  }

  @Test
  void labelFromKey_missingSpecificForm_fallsBackToPlural() throws Exception {
    assertEquals("tygodnie",
        labelFromKey("html", "5", entry(WEEK, "tydzień") + entry(WEEK + ".plural", "tygodnie")));
  }

  @Test
  void labelFromKey_missingPlural_fallsBackToSingular() throws Exception {
    assertEquals("tydzień", labelFromKey("json", "5", entry(WEEK, "tydzień")));
  }

  @Test
  void labelFromKey_missingLabel_displaysTheKey() throws Exception {
    assertEquals("{" + WEEK + "}", labelFromKey("html", "5", ""));
  }

  /**
   * In label_from_expression the labels are rendered inside a for-each over the label keys, where
   * the context item is a string. A quantity written as a relative path must still work.
   */
  @Test
  void labelFromExpression_relativeQuantity_selectsTheSpecificForm() throws Exception {
    for (String format : new String[] {"html", "json"}) {
      final String markup = render("xsl-markup/" + format + "/label_from_expression.ftl",
          Map.of("expression", "('" + WEEK + "')", "variableSuffix", "1", "quantity",
              "q/number()"));
      final String result = transform(
          stylesheet(labels(entry(WEEK, "tydzień") + entry(WEEK + ".few", "tygodnie")) + markup),
          "<root><q>3</q></root>", "pl");
      assertEquals("tygodnie", result, format);
    }
  }

  private static String labelFromKey(String format, String quantity, String entries)
      throws Exception {
    final String markup = render("xsl-markup/" + format + "/label_from_key.ftl",
        Map.of("key", "'" + WEEK + "'", "quantity", "/root/q/number()"));
    return transform(stylesheet(labels(entries) + markup),
        "<root><q>" + quantity + "</q></root>", "pl");
  }

  private static String entry(String key, String text) {
    return "<entry key=\"" + key + "\">" + text + "</entry>";
  }

  private static String labels(String entries) {
    return "<xsl:variable name=\"labels\"><properties>" + entries + "</properties></xsl:variable>";
  }

  private static String render(String template, Map<String, Object> model) throws Exception {
    try (StringWriter writer = new StringWriter()) {
      freemarker.getTemplate(template).process(model, writer);
      return writer.toString();
    }
  }

  /**
   * Builds a stylesheet that contains efx:plural-label-suffix, as included by the output file
   * templates, and the given content in the template matching the root element.
   */
  private static String stylesheet(String content) throws Exception {
    return "<xsl:stylesheet version=\"3.0\""
        + " xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\""
        + " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
        + " xmlns:efx=\"http://ted.europa.eu/efx\">"
        + "<xsl:output method=\"text\"/>"
        + "<xsl:param name=\"LANGUAGE\"/>"
        + render("xsl-markup/common/plural_label_suffix.ftl", Map.of())
        + "<xsl:template match=\"/root\">" + content + "</xsl:template>"
        + "</xsl:stylesheet>";
  }

  private static String transform(String xsl, String xml, String language) throws Exception {
    final Transformer transformer = TransformerFactory.newInstance()
        .newTransformer(new StreamSource(new StringReader(xsl)));
    transformer.setParameter("LANGUAGE", language);
    final StringWriter writer = new StringWriter();
    transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(writer));
    return writer.toString().trim();
  }
}
