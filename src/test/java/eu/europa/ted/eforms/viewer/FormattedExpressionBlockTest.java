package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.NoticeDocument;
import eu.europa.ted.eforms.viewer.generator.XslGenerator;
import eu.europa.ted.eforms.viewer.util.xml.TranslationUriResolver;
import eu.europa.ted.efx.EfxTranslatorOptions;
import eu.europa.ted.efx.interfaces.TranslatorOptions;
import eu.europa.ted.efx.model.DecimalFormat;

/**
 * Renders EFX 2 templates containing expression blocks, with and without format options, and checks
 * the text displayed for the values of a real notice.
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true",
    disabledReason = "Disabled on CI: Downloads SDK from internet")
@DisabledIfEnvironmentVariable(named = "bamboo_buildKey", matches = ".*",
    disabledReason = "Disabled on Bamboo: Downloads SDK from internet")
class FormattedExpressionBlockTest {

  private static final String SDK_VERSION = "2.0";
  private static final Path SDK_ROOT_DIR = NoticeViewerConstants.DEFAULT_SDK_ROOT_DIR;
  private static final Path NOTICE =
      Path.of("src", "test", "resources", "xml", SDK_VERSION, "16_cn_24_maximal-test.xml");

  @BeforeAll
  public static void setUp() {
    System.setProperty(NoticeViewerConstants.TEMPLATES_ROOT_DIR_PROPERTY, "target/templates");
  }

  // The notice has an estimated value of 9999999.99 EUR (BT-27-Procedure), was issued on
  // 2023-03-23+01:00 at 20:59:32+01:00 (BT-05), and has eight lots with a submission deadline on
  // 2023-05-22+01:00 (BT-131(d)-Lot), 60 days after the notice was issued. Two of the lots have a
  // planned duration (BT-36-Lot): 43 and 3 months. The SDK has no plural labels for the units yet. As a
  // contract notice, it has no results: the total value of the notice results (BT-161-NoticeResult) is
  // absent.

  @Test
  void testNumberWithDecimals() throws Exception {
    assertEquals("9,999,999.99", render("${BT-27-Procedure|2 no-unit}"));
  }

  @Test
  void testNumberWithoutDecimals_IsRounded() throws Exception {
    assertEquals("10,000,000", render("${BT-27-Procedure|0 no-unit}"));
  }

  @Test
  void testNumberWithoutDecimals_HasUpToNine() throws Exception {
    assertEquals("9,999,999.99", render("${BT-27-Procedure|no-unit}"));
  }

  @Test
  void testNumberWithConfiguredSeparators() throws Exception {
    assertEquals("9 999 999,99",
        render("ND-Root", "${BT-27-Procedure|2 no-unit}", new EfxTranslatorOptions(DecimalFormat.EFX_DEFAULT)));
  }

  @Test
  void testAmount_DisplaysItsCurrency() throws Exception {
    assertEquals("9,999,999.99 Euro", render("${BT-27-Procedure|2}"));
  }

  @Test
  void testAmountWithoutOptions_DisplaysItsCurrency() throws Exception {
    assertEquals("9,999,999.99 Euro", render("${BT-27-Procedure}"));
  }

  @Test
  void testExpression() throws Exception {
    assertEquals("20,000,000", render("${BT-27-Procedure * 2|0}"));
  }

  @Test
  void testDateShort() throws Exception {
    assertEquals("23/03/2023", render("${BT-05(a)-notice|short}"));
  }

  @Test
  void testDateMedium() throws Exception {
    assertEquals("23 Mar 2023", render("${BT-05(a)-notice|medium}"));
  }

  @Test
  void testDateLong() throws Exception {
    assertEquals("23 March 2023", render("${BT-05(a)-notice|long}"));
  }

  @Test
  void testDateWithoutOptions_IsShort() throws Exception {
    assertEquals("23/03/2023", render("${BT-05(a)-notice}"));
  }

  @Test
  void testTimeShort() throws Exception {
    assertEquals("20:59 +01:00", render("${BT-05(b)-notice|short}"));
  }

  @Test
  void testTimeMedium() throws Exception {
    assertEquals("20:59:32", render("${BT-05(b)-notice|medium}"));
  }

  @Test
  void testTimeLong() throws Exception {
    assertEquals("20:59:32 +01:00", render("${BT-05(b)-notice|long}"));
  }

  @Test
  void testListOfDates_FormatsEachValue() throws Exception {
    assertEquals(String.join(", ", Collections.nCopies(8, "22 May 2023")),
        render("${BT-131(d)-Lot|medium}"));
  }

  @Test
  void testDuration_DisplaysItsUnit() throws Exception {
    assertEquals("43 Month 3 Month", render("BT-36-Lot", "$value", NoticeViewerConstants.DEFAULT_TRANSLATOR_OPTIONS));
  }

  @Test
  void testCalculatedDuration_DisplaysMonths() throws Exception {
    assertEquals("43 Month", render("${BT-36-Lot[1]}"));
  }

  @Test
  void testCalculatedDuration_DisplaysDays() throws Exception {
    assertEquals("60 Day", render("${BT-131(d)-Lot[1] - BT-05(a)-notice}"));
  }

  @Test
  void testDurationsWithoutUnit_DisplayTheirNumbers() throws Exception {
    assertEquals("43, 3", render("${BT-36-Lot|no-unit}"));
  }

  @Test
  void testAbsentAmount_DisplaysNothing() throws Exception {
    assertEquals("", render("${BT-161-NoticeResult}"));
  }

  @Test
  void testAbsentNumber_DisplaysNothing() throws Exception {
    assertEquals("", render("${BT-161-NoticeResult|2 no-unit}"));
  }

  @Test
  void testAbsentCalculatedNumber_DisplaysNothing() throws Exception {
    assertEquals("", render("${BT-161-NoticeResult * 2}"));
  }

  @Test
  void testAttributeField() throws Exception {
    assertEquals("EUR", render("${BT-27-Procedure-Currency}"));
  }

  @Test
  void testContextAttributeFieldValue() throws Exception {
    assertEquals("EUR",
        render("BT-27-Procedure-Currency", "$value", NoticeViewerConstants.DEFAULT_TRANSLATOR_OPTIONS));
  }

  @Test
  void testNoFormatting_Number() throws Exception {
    assertEquals("9999999.99", render("${BT-27-Procedure|no-formatting}"));
  }

  @Test
  void testNoFormatting_Date() throws Exception {
    assertEquals("2023-03-23+01:00", render("${BT-05(a)-notice|no-formatting}"));
  }

  private static String render(String block)
      throws IOException, TransformerException, SAXException, ParserConfigurationException {
    return render("ND-Root", block, NoticeViewerConstants.DEFAULT_TRANSLATOR_OPTIONS);
  }

  /**
   * Renders a template line with the given context and block for the test notice, and returns the
   * text displayed, as a browser shows it: adjacent elements are not separated by spaces, so a missing
   * space between a value and its unit shows. The viewer displays the spaces of free text as
   * punctuation spaces (U+2008), which are compared here as ordinary spaces.
   */
  private static String render(String context, String block, TranslatorOptions options)
      throws IOException, TransformerException, SAXException, ParserConfigurationException {
    final Path xsl = XslGenerator.Builder
        .create(new DependencyFactoryForUnitTesting(SDK_ROOT_DIR))
        .build()
        .generateFile(SDK_VERSION, "expression-block-test", "{" + context + "} " + block + "\n", options, true);
    final String html = NoticeViewer.Builder
        .create()
        .withXsltProfiler(false)
        .withEfxProfiler(false)
        .withUriResolver(new TranslationUriResolver(SDK_VERSION, SDK_ROOT_DIR))
        .build()
        .generateHtmlString("en", "expression-block-test",
            new NoticeDocument(Files.readString(NOTICE, NoticeViewerConstants.DEFAULT_CHARSET)),
            Files.readString(xsl, NoticeViewerConstants.DEFAULT_CHARSET));
    return Jsoup.parse(html).body().text().replace('\u2008', ' ').replaceAll("\\s+", " ").trim();
  }
}
