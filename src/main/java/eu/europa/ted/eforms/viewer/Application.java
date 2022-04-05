package eu.europa.ted.eforms.viewer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  /**
   * @param args Command line arguments. See usage.
   *
   * @throws IOException If an error occurs during input or output
   * @throws ParserConfigurationException Error related to XML reader configuration
   * @throws SAXException XML parse error related
   * @throws TransformerException XSL transformation related errors
   */
  public static void main(final String[] args)
      throws IOException, SAXException, ParserConfigurationException, TransformerException {
    logger.info("eForms Notice Viewer");
    logger.info("Usage: <xml file to view> [<view id to use>]");
    logger.info("args={}", Arrays.toString(args));

    if (args.length < 1 || args.length > 2) {
      throw new RuntimeException("Invalid number of arguments, see usage.");
    }

    // We do not need the first arg anymore.
    final String cmdLnNoticeXml = args[0];
    final Optional<String> viewIdOpt = args.length > 1 ? Optional.of(args[1]) : Optional.empty();

    // TODO should we toleratate "X02_registration.xml" or without xml or both???
    // TODO not sure if we should allow passing an entire path (any path or just ids)
    generateView(cmdLnNoticeXml, viewIdOpt);
  }

  /**
   * @param noticeXml
   * @param viewIdOpt
   *
   * @throws IOException If an error occurs during input or output
   * @throws ParserConfigurationException Error related to XML reader configuration
   * @throws SAXException XML parse related errors
   * @throws TransformerException XSL transformation related errors
   */
  private static void generateView(final String noticeXml, final Optional<String> viewIdOpt)
      throws IOException, SAXException, ParserConfigurationException, TransformerException {
    try {
      final Path xslPath = NoticeViewer.parseNotice(viewIdOpt, noticeXml);
      logger.info("Created XSL file: {}", xslPath);

    } catch (IOException e) {
      logger.error(e.toString(), e);
      throw e;
    } catch (SAXException e) {
      logger.error(e.toString(), e);
      throw e;
    } catch (ParserConfigurationException e) {
      logger.error(e.toString(), e);
      throw e;
    }
  }

}
