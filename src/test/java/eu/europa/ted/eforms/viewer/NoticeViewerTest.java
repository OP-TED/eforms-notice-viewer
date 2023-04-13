package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.viewer.util.LoggingHelper;

@SuppressWarnings("static-method")
class NoticeViewerTest {
  private static final Logger logger = LoggerFactory.getLogger(NoticeViewerTest.class);

  private static final String[] SOURCE_LANGUAGES = new String[] {"en", "el"};

  private static final String[] SOURCE_NOTICE_XML_FILENAMES1 =
      new String[] {"16_cn_24_minimal-test"};

  private static final String[] SOURCE_NOTICE_XML_FILENAMES2 =
      new String[] {"16_cn_24_minimal-test"};

  private static final String[] SOURCE_SDK_VERSIONS = new String[] {"1.6"};

  private static final Path SDK_ROOT_DIR = NoticeViewerConstants.DEFAULT_SDK_ROOT_DIR;

  @BeforeAll
  public static void setUp() {
    System.setProperty(NoticeViewerConstants.TEMPLATES_ROOT_DIR_PROPERTY, "target/templates");
    LoggingHelper.installJulToSlf4jBridge();
  }

  private static Stream<Arguments> provideArgsEfxToHtml() {
    List<Arguments> arguments = new ArrayList<>();
    for (String language : SOURCE_LANGUAGES) {
      for (String noticeXmlFilename : SOURCE_NOTICE_XML_FILENAMES1) {
        for (String sdkVersion : SOURCE_SDK_VERSIONS) {
          arguments.add(Arguments.of(language, noticeXmlFilename, sdkVersion));
        }
      }
    }
    return Stream.of(arguments.toArray(new Arguments[0]));
  }

  private static Stream<Arguments> provideArgsEfxToHtmlFromString() {
    List<Arguments> arguments = new ArrayList<>();

    for (String language : SOURCE_LANGUAGES) {
      for (String noticeXmlFilename : SOURCE_NOTICE_XML_FILENAMES2) {
        for (String sdkVersion : SOURCE_SDK_VERSIONS) {
          String viewId = noticeXmlFilename.replaceAll("(^.*?)_.*", "$1");
          arguments.add(Arguments.of(language, noticeXmlFilename, viewId, sdkVersion));
        }
      }
    }

    return Stream.of(arguments.toArray(new Arguments[0]));
  }

  private static Stream<Arguments> provideArgsEfxToXsl() {
    return Arrays.asList(SOURCE_SDK_VERSIONS).stream().map((String s) -> Arguments.of(s));
  }

  @ParameterizedTest
  @MethodSource("provideArgsEfxToHtml")
  void testEfxToHtml(String language, String noticeXmlFilename, String sdkVersion)
      throws IOException, SAXException, ParserConfigurationException, InstantiationException {
    testGenerateHtmlFromFile(language, noticeXmlFilename, sdkVersion);
  }

  @ParameterizedTest
  @MethodSource("provideArgsEfxToHtmlFromString")
  void testEfxToHtmlFromString(String language, String noticeXmlFilename, String viewId,
      String sdkVersion)
      throws IOException, SAXException, ParserConfigurationException, InstantiationException {
    testGenerateHtmlFromString(language, noticeXmlFilename, viewId, sdkVersion);
  }

  @ParameterizedTest
  @MethodSource("provideArgsEfxToXsl")
  void testEfxToXsl(String sdkVersion) throws IOException, InstantiationException {
    final String viewId = "X02";
    final Path xsl = NoticeViewer.buildXsl(viewId, sdkVersion, SDK_ROOT_DIR, true);

    logger.info("TEST: Wrote file: {}", xsl);

    assertTrue(Files.isRegularFile(xsl));

    // labels are numbered randomly, so we remove those numbers before the comparison
    assertEquals(
        Files.readString(Path.of("src", "test", "resources", "xsl", sdkVersion,
            MessageFormat.format("{0}.xsl", viewId))),
        Files.readString(xsl).replaceAll("\\$labels?.*?,", "\\$label,")
            .replaceAll("(name=\"labels?).*?(\")", "$1$2"));
  }

  private Path getNoticeXmlPath(final String noticeXmlName, String sdkVersion) {
    return Path.of("src", "test", "resources", "xml", sdkVersion, noticeXmlName + ".xml");
  }

  private void testGenerateHtmlFromFile(final String language, final String noticeXmlName,
      final String sdkVersion)
      throws IOException, SAXException, ParserConfigurationException, InstantiationException {
    Path noticeXmlPath = getNoticeXmlPath(noticeXmlName, sdkVersion);
    final Optional<String> viewIdOpt = Optional.empty(); // Equivalent to not
                                                         // passing any in cli.
    final Path path =
        NoticeViewer.generateHtml(language, noticeXmlPath, viewIdOpt, false, SDK_ROOT_DIR,
            true);
    logger.info("TEST: Wrote html file: {}", path);

    assertTrue(Files.isRegularFile(path));
    assertEquals(Files.readString(Path.of("src", "test", "resources", "html", sdkVersion,
        MessageFormat.format("{0}-{1}.html", language, noticeXmlName))), Files.readString(path));
  }

  private void testGenerateHtmlFromString(final String language, final String noticeXmlName,
      final String viewId, final String sdkVersion)
      throws IOException, SAXException, ParserConfigurationException, InstantiationException {
    final Charset charsetUtf8 = StandardCharsets.UTF_8;
    Path noticeXmlPath = getNoticeXmlPath(noticeXmlName, sdkVersion);
    final String noticeXmlContent = Files.readString(noticeXmlPath, charsetUtf8);
    final Path xslPath = NoticeViewer.buildXsl(viewId, sdkVersion, SDK_ROOT_DIR, true);
    final String xslContent = Files.readString(xslPath, charsetUtf8);
    final String html = NoticeViewer.generateHtml(language, noticeXmlContent, xslContent,
        charsetUtf8, Optional.of(viewId), false, SDK_ROOT_DIR);

    logger.info("TEST: Wrote html {} ...", StringUtils.left(html, 50));

    assertEquals(Files.readString(Path.of("src", "test", "resources", "html", sdkVersion,
        MessageFormat.format("{0}-{1}-{2}.html", language, viewId, noticeXmlName))), html);
  }
}
