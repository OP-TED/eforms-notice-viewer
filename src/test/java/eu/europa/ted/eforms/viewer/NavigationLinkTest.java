package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.NoticeDocument;
import eu.europa.ted.eforms.viewer.generator.XslGenerator;
import eu.europa.ted.eforms.viewer.util.xml.TranslationUriResolver;
import eu.europa.ted.efx.EfxTranslator;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;

/**
 * Renders EFX 2 templates with a navigation section for a notice with several lots, as an HTML page
 * and as JSON, and checks that each navigation entry links to its own heading: the heading of the
 * main section with the same outline address that is displayed for the same element.
 */
@DisabledIfEnvironmentVariable(named = "CI", matches = "true",
    disabledReason = "Disabled on CI: Downloads SDK from internet")
@DisabledIfEnvironmentVariable(named = "bamboo_buildKey", matches = ".*",
    disabledReason = "Disabled on Bamboo: Downloads SDK from internet")
class NavigationLinkTest {

  private static final String SDK_VERSION = "2.0";
  private static final Path SDK_ROOT_DIR = NoticeViewerConstants.DEFAULT_SDK_ROOT_DIR;
  private static final Path NOTICE =
      Path.of("src", "test", "resources", "xml", SDK_VERSION, "16_cn_24_maximal-test.xml");

  // A tagged heading displayed once, a numbered heading displayed for each lot, and a numbered
  // heading nested in it, whose number is not displayed. The navigation section repeats them, and
  // the summary section displays a numbered line for each lot.
  private static final String TEMPLATE = String.join("\n",
      "#notice-info WITH ND-Root DISPLAY Notice information;",
      "1 WITH ND-Lot DISPLAY Lot ${BT-137-Lot};",
      "\t1 WITH BT-21-Lot DISPLAY Title ${BT-137-Lot};",
      "--- NAVIGATION ---",
      "#notice-info WITH ND-Root DISPLAY Notice information;",
      "1 WITH ND-Lot DISPLAY Lot ${BT-137-Lot};",
      "\t1 WITH BT-21-Lot DISPLAY Title ${BT-137-Lot};",
      "--- SUMMARY ---",
      "1 WITH ND-Lot DISPLAY Lot ${BT-137-Lot};",
      "");

  @BeforeAll
  public static void setUp() {
    System.setProperty(NoticeViewerConstants.TEMPLATES_ROOT_DIR_PROPERTY, "target/templates");
  }

  @Test
  void testEachNavigationEntry_LinksToItsOwnHeading() throws Exception {
    final Document html = Jsoup.parse(renderHtml(TEMPLATE));
    final List<Element> links = html.select(".nav-content a[href^=#]");
    for (Element link : links) {
      final Element heading = html.getElementById(link.attr("href").substring(1));
      assertTrue(heading != null, "No heading for " + link.attr("href"));
      assertTrue(heading.closest(".body") != null,
          "The heading of " + link.attr("href") + " is not in the main section");
      assertTrue(heading.text().startsWith(link.text()),
          "'" + link.text() + "' links to '" + heading.text() + "'");
    }
    // Every heading of the main section is linked to, each by one entry.
    assertEquals(hrefsOfHeadingsIn(html, ".body"), hrefsOf(links));
  }

  @Test
  void testNoTwoElements_HaveTheSameId() throws Exception {
    final List<String> ids = Jsoup.parse(renderHtml(TEMPLATE)).select("[id]").stream()
        .map(Element::id).collect(Collectors.toList());
    assertEquals(ids.size(), ids.stream().distinct().count());
  }

  @Test
  void testNavigationEntriesAndSummaryLines_HaveNoId() throws Exception {
    final Document html = Jsoup.parse(renderHtml(TEMPLATE));
    assertTrue(html.select(".nav-content section[id]").isEmpty());
    assertTrue(html.select(".summary-content section[id]").isEmpty());
  }

  @Test
  void testATemplateInvokedFromEverySection_HasItsIdInTheMainSectionOnly() throws Exception {
    // A template is displayed as a heading in the main section, as a navigation entry in the
    // navigation section, and as a line in the summary section.
    final Document html = Jsoup.parse(renderHtml(String.join("\n",
        "--- TEMPLATE info ---",
        "#notice-info WITH ND-Root DISPLAY Notice information;",
        "--- MAIN ---",
        "INVOKE info();",
        "--- SUMMARY ---",
        "INVOKE info();",
        "--- NAVIGATION ---",
        "INVOKE info();",
        "")));
    final List<Element> ids = html.select("[id^=notice-info-]");
    assertEquals(1, ids.size());
    assertTrue(ids.get(0).closest(".body") != null);
    final List<Element> links = html.select(".nav-content a");
    assertEquals(List.of("#" + ids.get(0).id()), hrefsOf(links));
  }

  @Test
  void testATemplateUnderTwoHeadings_IsLinkedToUnderEach() throws Exception {
    // The headings of the template continue the address of the heading they are invoked under.
    final Document html = Jsoup.parse(renderHtml(String.join("\n",
        "--- TEMPLATE details ---",
        "#details WITH ND-Root DISPLAY Details;",
        "--- MAIN ---",
        "1 WITH ND-Root DISPLAY Buyer;",
        "\tINVOKE details();",
        "2 WITH ND-Root DISPLAY Review body;",
        "\tINVOKE details();",
        "--- NAVIGATION ---",
        "1 WITH ND-Root DISPLAY Buyer;",
        "\tINVOKE details();",
        "2 WITH ND-Root DISPLAY Review body;",
        "\tINVOKE details();",
        "")));
    final List<String> details = html.select(".body [id*=details-]").stream()
        .map(Element::id).collect(Collectors.toList());
    assertEquals(2, details.size());
    assertTrue(details.get(0).startsWith("1_details-"), details.get(0));
    assertTrue(details.get(1).startsWith("2_details-"), details.get(1));
    assertEquals(hrefsOfHeadingsIn(html, ".body"), hrefsOf(html.select(".nav-content a")));
  }

  @Test
  void testAHeadingWithATemplateOfNormalLinesUnderIt_LinksAroundItsOwnContentOnly() throws Exception {
    // The lines of the template take the place of the invoking line, under the heading.
    final String template = String.join("\n",
        "--- TEMPLATE buyer-name ---",
        "WITH ND-Root DISPLAY Buyer name;",
        "--- MAIN ---",
        "#buyer WITH ND-Root DISPLAY Buyer;",
        "\tWITH ND-Root INVOKE buyer-name();",
        "--- NAVIGATION ---",
        "#buyer WITH ND-Root DISPLAY Buyer;",
        "\tWITH ND-Root INVOKE buyer-name();",
        "");
    final Document html = Jsoup.parse(renderHtml(template));
    final List<Element> links = html.select(".nav-content a");
    assertEquals(1, links.size());
    assertEquals("Buyer", textOf(links.get(0)));
    assertEquals("#" + html.selectFirst(".body [id^=buyer-]").id(), links.get(0).attr("href"));
    assertEquals("Buyer Buyer name", textOf(html.selectFirst(".nav-content")));
    final String json = renderJson(template);
    assertEquals("Buyer", spaced(jsonValue(json, "?navigation?1?label").getStringValue()));
    assertEquals("Buyer name",
        spaced(jsonValue(json, "?navigation?1?children?1?label").getStringValue()));
    assertTrue(jsonValues(json, "?navigation?1?children?1?url").isEmpty());
  }

  @Test
  void testATemplateOfHeadingsUnderAHeading_LinksToEachOfThem() throws Exception {
    // The headings of the template are addressed under the heading above the invoking line.
    final Document html = Jsoup.parse(renderHtml(String.join("\n",
        "--- TEMPLATE details ---",
        "#details WITH ND-Root DISPLAY Details;",
        "--- MAIN ---",
        "2 WITH ND-Root DISPLAY Second;",
        "\tWITH ND-Root INVOKE details();",
        "--- NAVIGATION ---",
        "2 WITH ND-Root DISPLAY Second;",
        "\tWITH ND-Root INVOKE details();",
        "")));
    final List<Element> links = html.select(".nav-content a");
    assertEquals(2, links.size());
    assertEquals("2 Second", textOf(links.get(0)));
    assertTrue(links.get(0).attr("href").startsWith("#2-"), links.get(0).attr("href"));
    assertEquals("Details", textOf(links.get(1)));
    assertTrue(links.get(1).attr("href").startsWith("#2_details-"), links.get(1).attr("href"));
  }

  @Test
  void testAHeadingWithANormalLineUnderIt_LinksAroundItsOwnContentOnly() throws Exception {
    final Document html = Jsoup.parse(renderHtml(String.join("\n",
        "#notice-info WITH ND-Root DISPLAY Notice information;",
        "--- NAVIGATION ---",
        "#notice-info WITH ND-Root DISPLAY Notice information;",
        "\tWITH ND-Root DISPLAY More;",
        "")));
    final Element link = html.selectFirst(".nav-content a");
    assertEquals("Notice information", textOf(link));
    assertTrue(textOf(html.selectFirst(".nav-content")).contains("More"));
    assertFalse(textOf(link).contains("More"));
  }

  @Test
  void testAHeadingThatDisplaysNothing_IsNotALink_AndNeitherIsTheLineUnderIt() throws Exception {
    final Document html = Jsoup.parse(renderHtml(String.join("\n",
        "#help WITH ND-Root DISPLAY Help;",
        "--- NAVIGATION ---",
        "#help WITH ND-Root DISPLAY ${''};",
        "\tWITH ND-Root DISPLAY More@{'https://example.com'};",
        "")));
    final List<Element> links = html.select(".nav-content a");
    assertEquals(1, links.size());
    assertEquals("https://example.com", links.get(0).attr("href"));
    assertTrue(html.select(".nav-content a a").isEmpty());
  }

  @Test
  void testTheTopLinesOfATemplateTakeThePlaceOfTheInvokingLine() throws Exception {
    // The template only holds lines: its top lines are in the section of the line above the
    // invoking line, one after the other, beside the next line.
    final Document html = Jsoup.parse(renderHtml(String.join("\n",
        "--- TEMPLATE names ---",
        "DISPLAY Buyer;",
        "DISPLAY Seller;",
        "--- MAIN ---",
        "1 WITH ND-Root DISPLAY A;",
        "\tWITH ND-Root INVOKE names();",
        "\tWITH ND-Root DISPLAY More;",
        "")));
    final Element heading = html.selectFirst(".body section[id^=1-]");
    assertEquals(List.of("names01", "names02", "body0102"), heading.children().stream()
        .filter(child -> child.tagName().equals("section"))
        .map(section -> section.attr("title")).collect(Collectors.toList()));
    assertTrue(html.select(".body section[title=names]").isEmpty());
    assertEquals("1 A Buyer Seller More", textOf(heading));
  }

  @Test
  void testAnInvokingLineWritesNoSectionOfItsOwn() throws Exception {
    // The template takes the place of the invoking line: its section is directly in the section of
    // the line above, beside the next line.
    final Document html = Jsoup.parse(renderHtml(String.join("\n",
        "--- TEMPLATE name ---",
        "DISPLAY Buyer;",
        "--- MAIN ---",
        "1 WITH ND-Root DISPLAY A;",
        "\tWITH ND-Root INVOKE name();",
        "\tWITH ND-Root DISPLAY More;",
        "")));
    final Element heading = html.selectFirst(".body section[id^=1-]");
    assertEquals(List.of("name", "body0102"), heading.children().stream()
        .filter(child -> child.tagName().equals("section"))
        .map(section -> section.attr("title")).collect(Collectors.toList()));
    assertTrue(html.select(".body section[title=body0101]").isEmpty());
  }

  @Test
  void testALineThatInvokesInEachAlternative_WritesNoSectionOfItsOwn() throws Exception {
    // WHEN c INVOKE a() OTHERWISE INVOKE b(): the template of the alternative that applies takes the
    // place of the line, beside the next line.
    for (final String condition : List.of("1 == 1", "1 == 2")) {
      final Document html = Jsoup.parse(renderHtml(String.join("\n",
          "--- TEMPLATE first ---",
          "DISPLAY Buyer;",
          "--- TEMPLATE second ---",
          "DISPLAY Seller;",
          "--- MAIN ---",
          "1 WITH ND-Root DISPLAY A;",
          "\tWITH ND-Root WHEN " + condition + " INVOKE first() OTHERWISE INVOKE second();",
          "\tWITH ND-Root DISPLAY More;",
          "")));
      final boolean first = condition.equals("1 == 1");
      final Element heading = html.selectFirst(".body section[id^=1-]");
      assertEquals(List.of(first ? "first" : "second", "body0102"), heading.children().stream()
          .filter(child -> child.tagName().equals("section"))
          .map(section -> section.attr("title")).collect(Collectors.toList()));
      assertTrue(html.select(".body section[title=body0101]").isEmpty());
      assertEquals(first ? "1 A Buyer More" : "1 A Seller More", textOf(heading));
    }
  }

  @Test
  void testANormalLineOfTheNavigation_IsDisplayedAsItIs() throws Exception {
    final Document html = Jsoup.parse(renderHtml(String.join("\n",
        "#notice-info WITH ND-Root DISPLAY Notice information;",
        "--- NAVIGATION ---",
        "WITH ND-Root DISPLAY Contents;",
        "")));
    assertEquals("Contents", textOf(html.selectFirst(".nav-content")));
    assertTrue(html.select(".nav-content a").isEmpty());
  }

  @Test
  void testJson_EachNavigationEntry_LinksToItsOwnHeading() throws Exception {
    final String json = renderJson(TEMPLATE);
    final Document body = Jsoup.parse(jsonValue(json, "?body").getStringValue());
    final List<String> urls = new ArrayList<>();
    for (XdmItem url : jsonValues(json, "?navigation?*!(., ?children?*)?url")) {
      urls.add(url.getStringValue());
    }
    assertEquals(hrefsOfHeadingsIn(body, "body"), urls.stream().sorted().collect(Collectors.toList()));
    assertEquals("Notice information", spaced(jsonValue(json, "?navigation?1?label").getStringValue()));
  }

  @Test
  void testJson_IsValid_ForNumberedEntriesThatRepeatAndHaveChildren() throws Exception {
    // One entry for each lot, each with a child: a parser reads it, with one url for each heading.
    final String json = renderJson(TEMPLATE);
    assertEquals("1 Lot LOT-0001", spaced(jsonValue(json, "?navigation?2?label").getStringValue()));
    assertTrue(jsonValue(json, "?navigation?2?children?1?url").getStringValue().startsWith("#1_1-"));
    assertTrue(jsonValues(json, "?navigation?*").size() > 2);
  }

  @Test
  void testJson_AnEmptyValue_DoesNotHoldTheLinesAfterIt() throws Exception {
    // The body and the summary are read as HTML: an empty element written as <span/> would be
    // taken for an opening tag, and the lines after it would be displayed inside it.
    final String json = renderJson(String.join("\n",
        "WITH ND-Root DISPLAY ${''};",
        "WITH ND-Root DISPLAY After;",
        "--- SUMMARY ---",
        "WITH ND-Root DISPLAY ${''};",
        "WITH ND-Root DISPLAY After;",
        ""));
    for (String part : List.of("?body", "?summary")) {
      final String html = jsonValue(json, part).getStringValue();
      assertFalse(html.contains("/>"), html);
      final Document parsed = Jsoup.parse(html);
      assertTrue(parsed.select("span section").isEmpty(), html);
      assertEquals(2, parsed.select("body > section").size(), html);
    }
  }

  @Test
  void testJson_IsValid_ForATemplateWithoutSummaryOrNavigation() throws Exception {
    // As the view templates of the SDK are today.
    final String json = renderJson(String.join("\n",
        "1 WITH ND-Root DISPLAY Buyer;",
        ""));
    assertTrue(Jsoup.parse(jsonValue(json, "?body").getStringValue()).text().contains("Buyer"));
    assertEquals("", jsonValue(json, "?summary").getStringValue());
    assertTrue(jsonValues(json, "?navigation?*").isEmpty());
  }

  @Test
  void testJson_SummaryHasNoIds() throws Exception {
    final String json = renderJson(TEMPLATE);
    assertTrue(Jsoup.parse(jsonValue(json, "?summary").getStringValue()).select("[id]").isEmpty());
  }

  /**
   * @return the text displayed, as a browser shows it: the viewer displays the spaces of free text
   *         as punctuation spaces (U+2008), and the space after an outline number as a no-break
   *         space, which are compared here as ordinary spaces
   */
  private static String textOf(final Element element) {
    return spaced(element.text());
  }

  private static String spaced(final String text) {
    return text.replace('\u2008', ' ').replace('\u00a0', ' ').replaceAll("\\s+", " ").trim();
  }

  private static List<String> hrefsOf(final List<Element> links) {
    return links.stream().map(link -> link.attr("href")).sorted().collect(Collectors.toList());
  }

  private static List<String> hrefsOfHeadingsIn(final Document html, final String container) {
    return html.select(container + " section[id]").stream().map(heading -> "#" + heading.id())
        .sorted().collect(Collectors.toList());
  }

  private static XdmItem jsonValue(final String json, final String path) throws SaxonApiException {
    return jsonValues(json, path).get(0);
  }

  /**
   * @return the values at the path in the JSON, read with the XPath function parse-json, which
   *         fails on anything that is not valid JSON
   */
  private static List<XdmItem> jsonValues(final String json, final String path)
      throws SaxonApiException {
    final XPathCompiler compiler = new Processor(false).newXPathCompiler();
    compiler.declareVariable(new QName("json"));
    final XPathSelector selector = compiler.compile("parse-json($json)" + path).load();
    selector.setVariable(new QName("json"), new XdmAtomicValue(json));
    final List<XdmItem> values = new ArrayList<>();
    selector.evaluate().forEach(values::add);
    return values;
  }

  private static String renderHtml(final String template)
      throws IOException, TransformerException, SAXException, ParserConfigurationException {
    final Path xsl = XslGenerator.Builder
        .create(new DependencyFactoryForUnitTesting(SDK_ROOT_DIR))
        .build()
        .generateFile(SDK_VERSION, "navigation-link-test", template,
            NoticeViewerConstants.DEFAULT_TRANSLATOR_OPTIONS, true);
    return render(Files.readString(xsl, NoticeViewerConstants.DEFAULT_CHARSET));
  }

  private static String renderJson(final String template) throws Exception {
    return render(EfxTranslator.translateTemplate(
        new DependencyFactoryForUnitTesting(SDK_ROOT_DIR), SDK_VERSION, "json", template,
        NoticeViewerConstants.DEFAULT_TRANSLATOR_OPTIONS));
  }

  private static String render(final String xsl)
      throws IOException, TransformerException, SAXException, ParserConfigurationException {
    return NoticeViewer.Builder
        .create()
        .withXsltProfiler(false)
        .withEfxProfiler(false)
        .withUriResolver(new TranslationUriResolver(SDK_VERSION, SDK_ROOT_DIR))
        .build()
        .generateHtmlString("en", "navigation-link-test",
            new NoticeDocument(Files.readString(NOTICE, NoticeViewerConstants.DEFAULT_CHARSET)),
            xsl);
  }
}
