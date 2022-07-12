package eu.europa.ted.eforms.viewer;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.Callable;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.viewer.helpers.SdkResourceLoader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "view", mixinStandardHelpOptions = true, description = "eForms Notice Viewer")
class CliCommand implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(CliCommand.class);

  @Spec
  CommandSpec spec; // injected by picocli

  private String language;

  @Parameters(index = "1", description = "Path of XML file to view.")
  private Path noticeXmlPath;

  @Option(names = {"-i", "--viewId"}, description = "View ID to use.")
  private String viewId;

  @Option(names = {"-r", "--sdkRoot"}, description = "SDK resources root folder.")
  private String sdkResourcesRoot;

  @Parameters(index = "0", description = "Two letter language code.")
  public void setLanguage(String language) {
    if (StringUtils.isBlank(language) || language.length() != 2) {
      throw new ParameterException(spec.commandLine(),
          MessageFormat.format(
              "Language: expecting two letter code like 'en', 'fr', ..., but found \'\'{0}\'\'",
              language));
    }
    this.language = language;
  }

  /**
   * @param args Command line arguments. See usage.
   *
   * @throws IOException If an error occurs during input or output
   * @throws ParserConfigurationException Error related to XML reader configuration
   * @throws SAXException XML parse error related
   * @throws InstantiationException 
   */
  @Override
  public Integer call() throws IOException, SAXException, ParserConfigurationException, InstantiationException {
    SdkResourceLoader.INSTANCE.setRoot(sdkResourcesRoot);

    final Path htmlPath =
        NoticeViewer.generateHtml(language, noticeXmlPath, Optional.ofNullable(viewId));
    logger.info("Created HTML file: {}", htmlPath);

    return 0;
  }
}
