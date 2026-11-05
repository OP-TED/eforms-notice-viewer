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

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;

/**
 * Tests efx:preferred-language and efx:preferred-language-text (TEDEFO-2892): they select one
 * language, and one text, for each value of a multilingual field, also when the elements come
 * from several parents.
 *
 * The tests render the bundled Freemarker template and run the resulting XSLT with Saxon, the
 * same processor used by the notice viewer.
 */
class PreferredLanguageTest {

  /** Two EU funds, each with a description in English and in German, as in TEDEFO-2892. */
  private static final String TWO_FUNDS = "<root>"
      + "<Funding><Description languageID=\"ENG\">Recovery Fund</Description>"
      + "<Description languageID=\"DEU\">Aufbaufonds</Description></Funding>"
      + "<Funding><Description languageID=\"ENG\">This local project</Description>"
      + "<Description languageID=\"DEU\">Dieses lokale Projekt</Description></Funding>"
      + "</root>";

  /** The same two EU funds, but the second one has no German description. */
  private static final String GERMAN_MISSING_IN_SECOND_FUND = "<root>"
      + "<Funding><Description languageID=\"ENG\">Recovery Fund</Description>"
      + "<Description languageID=\"DEU\">Aufbaufonds</Description></Funding>"
      + "<Funding><Description languageID=\"ENG\">This local project</Description></Funding>"
      + "</root>";

  private static Configuration freemarker;

  @BeforeAll
  static void setUp() {
    System.setProperty("javax.xml.transform.TransformerFactory",
        "net.sf.saxon.TransformerFactoryImpl");
    freemarker = new Configuration(Configuration.VERSION_2_3_31);
    freemarker.setTemplateLoader(new ClassTemplateLoader(PreferredLanguageTest.class, "/templates"));
  }

  @Test
  void preferredLanguageText_severalParents_givesOneTextForEach() throws Exception {
    assertEquals("Aufbaufonds|Dieses lokale Projekt",
        select("efx:preferred-language-text(//Description)", "'DEU', 'ENG'", TWO_FUNDS));
    assertEquals("Recovery Fund|This local project",
        select("efx:preferred-language-text(//Description)", "'ENG', 'DEU'", TWO_FUNDS));
  }

  @Test
  void preferredLanguage_severalParents_givesOneLanguageForEach() throws Exception {
    assertEquals("DEU|DEU",
        select("efx:preferred-language(//Description)", "'DEU', 'ENG'", TWO_FUNDS));
  }

  @Test
  void preferredLanguage_languageMissingInOneParent_takesTheNextLanguageForIt() throws Exception {
    assertEquals("DEU|ENG", select("efx:preferred-language(//Description)", "'DEU', 'ENG'",
        GERMAN_MISSING_IN_SECOND_FUND));
  }

  @Test
  void preferredLanguage_oneParent_givesOneLanguage() throws Exception {
    assertEquals("ENG",
        select("efx:preferred-language(//Funding[2]/Description)", "'ENG', 'DEU'", TWO_FUNDS));
  }

  @Test
  void preferredLanguage_noElement_givesNothing() throws Exception {
    assertEquals("", select("efx:preferred-language(//Missing)", "'DEU'", TWO_FUNDS));
  }

  @Test
  void preferredLanguageText_languageMissingInOneParent_takesTheNextLanguageForIt()
      throws Exception {
    assertEquals("Aufbaufonds|This local project",
        select("efx:preferred-language-text(//Description)", "'DEU', 'ENG'",
            GERMAN_MISSING_IN_SECOND_FUND));
  }

  @Test
  void preferredLanguageText_noPreferredLanguage_takesTheFirstElement() throws Exception {
    assertEquals("Recovery Fund|This local project",
        select("efx:preferred-language-text(//Description)", "'FRA'", TWO_FUNDS));
  }

  @Test
  void preferredLanguageText_oneParent_givesOneText() throws Exception {
    assertEquals("Aufbaufonds",
        select("efx:preferred-language-text(//Funding[1]/Description)", "'DEU', 'ENG'",
            TWO_FUNDS));
  }

  @Test
  void preferredLanguageText_noElement_givesNothing() throws Exception {
    assertEquals("", select("efx:preferred-language-text(//Missing)", "'DEU'", TWO_FUNDS));
  }

  /**
   * Evaluates the given expression with the given preferred languages, and returns its values
   * separated by '|'.
   */
  private static String select(String expression, String preferredLanguages, String xml)
      throws Exception {
    final String xsl = "<xsl:stylesheet version=\"3.0\""
        + " xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\""
        + " xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
        + " xmlns:efx=\"http://ted.europa.eu/efx\">"
        + "<xsl:output method=\"text\"/>"
        + "<xsl:variable name=\"PREFERRED_LANGUAGES\" select=\"(" + preferredLanguages
        + ")\" as=\"xs:string*\"/>"
        + render("xsl-markup/common/preferred_language.ftl")
        + "<xsl:template match=\"/root\"><xsl:value-of select=\"string-join(" + expression
        + ", '|')\"/></xsl:template>"
        + "</xsl:stylesheet>";
    final Transformer transformer = TransformerFactory.newInstance()
        .newTransformer(new StreamSource(new StringReader(xsl)));
    final StringWriter writer = new StringWriter();
    transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(writer));
    return writer.toString().trim();
  }

  private static String render(String template) throws Exception {
    try (StringWriter writer = new StringWriter()) {
      freemarker.getTemplate(template).process(Map.of(), writer);
      return writer.toString();
    }
  }
}
