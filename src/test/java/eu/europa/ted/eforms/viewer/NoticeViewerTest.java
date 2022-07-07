package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.viewer.helpers.SdkConstants;
import eu.europa.ted.eforms.viewer.helpers.SdkResourcesLoader;

class NoticeViewerTest {
  private static final Logger logger = LoggerFactory.getLogger(NoticeViewerTest.class);

  private static final String[] SOURCE_LANGUAGES = new String[] {"en", "el"};

  private static final String[] SOURCE_NOTICE_XML_FILENAMES1 =
      new String[] {"X01_EEIG", "X02_registration", "pin-buyer_24_minimal"};

  private static final String[] SOURCE_NOTICE_XML_FILENAMES2 =
      new String[] {"X01_EEIG", "X02_registration"};

  private static final String[] SOURCE_SDK_VERSIONS = new String[] {"0.6", "0.7"};

  private static final String SDK_RESOURCES_ROOT =
      Path.of("target", SdkConstants.DEFAULT_SDK_ROOT).toString();

  @BeforeEach
  public void setUp() {
    SdkResourcesLoader.INSTANCE.setRoot(SDK_RESOURCES_ROOT);
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
      throws IOException, SAXException, ParserConfigurationException {
    testGenerateHtmlFromFile(language, noticeXmlFilename, sdkVersion);
  }

  @ParameterizedTest
  @MethodSource("provideArgsEfxToHtmlFromString")
  void testEfxToHtmlFromString(String language, String noticeXmlFilename, String viewId,
      String sdkVersion) throws IOException, SAXException, ParserConfigurationException {
    testGenerateHtmlFromString(language, noticeXmlFilename, viewId, sdkVersion);
  }

  @ParameterizedTest
  @MethodSource("provideArgsEfxToXsl")
  void testEfxToXsl(String sdkVersion) throws IOException {
    final String viewId = "X02";
    final Path xsl = NoticeViewer.buildXsl(viewId, sdkVersion);
    logger.info("TEST: Wrote file: {}", xsl);
    assertTrue(xsl.toFile().exists());
    // The test would have failed if there were errors, this is what the check is really about.
  }

  private Path getNoticeXmlPath(final String noticeXmlName, String sdkVersion) {
    return Path.of("src", "test", "resources", "xml", sdkVersion, noticeXmlName + ".xml");
  }

  private void testGenerateHtmlFromFile(final String language, final String noticeXmlName,
      final String sdkVersion) throws IOException, SAXException, ParserConfigurationException {
    Path noticeXmlPath = getNoticeXmlPath(noticeXmlName, sdkVersion);
    final Optional<String> viewIdOpt = Optional.empty(); // Equivalent to not
                                                         // passing any in cli.
    final Path path = NoticeViewer.generateHtml(language, noticeXmlPath, viewIdOpt);
    logger.info("TEST: Wrote html file: {}", path);
    final File htmlFile = path.toFile();
    assertTrue(htmlFile.exists());
    // The test would have failed if there were errors, this is what the check
    // is really about.
  }

  private void testGenerateHtmlFromString(final String language, final String noticeXmlName,
      final String viewId, final String sdkVersion)
      throws IOException, SAXException, ParserConfigurationException {
    final Charset charsetUtf8 = StandardCharsets.UTF_8;
    Path noticeXmlPath = getNoticeXmlPath(noticeXmlName, sdkVersion);
    final String noticeXmlContent = Files.readString(noticeXmlPath, charsetUtf8);
    final Path xslPath = NoticeViewer.buildXsl(viewId, sdkVersion);
    final String xslContent = Files.readString(xslPath, charsetUtf8);
    final String html = NoticeViewer.generateHtml(language, noticeXmlContent, xslContent,
        charsetUtf8, Optional.of(viewId));
    logger.info("TEST: Wrote html {} ...", StringUtils.left(html, 50));
    assertTrue(StringUtils.isNotBlank(html));
    // The test would have failed if there were errors, this is what the check
    // is really about.
  }
}
