package eu.europa.ted.eforms.viewer;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import eu.europa.ted.eforms.viewer.helpers.SdkResourcesLoader;

/**
 * Entry point.
 */
public class Application {
  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  /**
   * @param args
   *          Command line arguments. See usage.
   *
   * @throws IOException
   *           If an error occurs during input or output
   * @throws ParserConfigurationException
   *           Error related to XML reader configuration
   * @throws SAXException
   *           XML parse error related
   */
  public static void main(final String[] args) throws IOException, SAXException, ParserConfigurationException {
    logger.info("eForms Notice Viewer");
    logger.info("Usage: <two letter language code> <path of XML file to view> [<view id to use>] [SDK resources root folder]");
    logger.info("Example: en X02_registration.xml");
    logger.info("args={}", Arrays.toString(args));
    if (args.length < 1 || args.length > 5) {
      throw new RuntimeException("Invalid number of arguments, see usage.");
    }
    final String language = args[0];
    if (language.length() != 2) {
      throw new RuntimeException(MessageFormat.format("Language: expecting two letter code like 'en', 'fr', ..., but found '{0}'", language));
    }
    final Path noticeXmlPath = Path.of(args[1]);
    final Optional<String> viewIdOpt = args.length > 2 ? Optional.of(args[2]) : Optional.empty();
    final Optional<String> sdkResourcesRoot = args.length > 3 ? Optional.of(args[4]) : Optional.empty();
    SdkResourcesLoader.getInstance().setRoot(sdkResourcesRoot);
    final Path htmlPath = NoticeViewer.generateHtml(language, noticeXmlPath, viewIdOpt);
    logger.info("Created HTML file: {}", htmlPath);
    System.exit(0);
  }
}
