package eu.europa.ted.eforms.viewer.cli;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.viewer.NoticeDocument;
import eu.europa.ted.eforms.viewer.NoticeViewer;
import eu.europa.ted.eforms.viewer.NoticeViewerConstants;
import eu.europa.ted.eforms.viewer.config.NoticeViewerConfig;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "", mixinStandardHelpOptions = true, description = "eForms Notice Viewer",
    versionProvider = CliCommand.ManifestVersionProvider.class)
public class CliCommand implements Callable<Integer> {
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

  @Option(names = {"-p", "--profileXslt"}, description = "Enable XSLT profiling.")
  private boolean profileXslt;

  @Option(names = {"-f", "--force"},
      description = "Force re-building of XSL by clearing any cached content.")
  private boolean forceBuild;

  @Option(names = {"-t", "--templatesRoot"}, description = "Templates root folder.")
  void setTemplatesRoot(String templatesRoot) {
    System.setProperty(NoticeViewerConstants.TEMPLATES_ROOT_DIR_PROPERTY, templatesRoot);
  }

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
   * @throws IOException If an error occurs during input or output
   * @throws ParserConfigurationException Error related to XML reader configuration
   * @throws SAXException XML parse error related
   * @throws InstantiationException
   * @throws URISyntaxException
   * @throws TransformerException
   * @throws XPathExpressionException
   */
  @Override
  public Integer call()
      throws IOException, SAXException, ParserConfigurationException, InstantiationException,
      URISyntaxException, TransformerException, XPathExpressionException {
    Validate.notNull(noticeXmlPath, "Undefined notice XML path");
    if (!Files.isRegularFile(noticeXmlPath)) {
      throw new FileNotFoundException(noticeXmlPath.toString());
    }

    final String xmlContents = Files.readString(noticeXmlPath);

    // Initialise Freemarker templates so that the templates folder will be populated
    NoticeViewerConfig.getFreemarkerConfig();

    final Path sdkRoot = Optional.ofNullable(sdkResourcesRoot)
        .map(Path::of)
        .orElse(NoticeViewerConstants.DEFAULT_SDK_ROOT_DIR);

    final Path htmlPath =
        NoticeViewer.Builder
            .create()
            .withProfileXslt(profileXslt)
            .build()
            .generateHtmlFile(language, viewId, new NoticeDocument(xmlContents), null, sdkRoot,
                NoticeViewerConstants.DEFAULT_TRANSLATOR_OPTIONS.getDecimalFormat(),
                forceBuild);

    logger.info("Created HTML file: {}", htmlPath);

    return 0;
  }

  /**
   * {@link IVersionProvider} implementation that returns version information from the
   * picocli-x.x.jar file's {@code /META-INF/MANIFEST.MF} file.
   */
  static class ManifestVersionProvider implements IVersionProvider {
    public String[] getVersion() throws Exception {
      Enumeration<URL> resources =
          CommandLine.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
      while (resources.hasMoreElements()) {
        URL url = resources.nextElement();
        try {
          Manifest manifest = new Manifest(url.openStream());
          if (isApplicableManifest(manifest)) {
            Attributes attr = manifest.getMainAttributes();
            return new String[] {get(attr, "Implementation-Title") + " version \""
                + get(attr, "Implementation-Version") + "\""};
          }
        } catch (IOException ex) {
          return new String[] {"Unable to read from " + url + ": " + ex};
        }
      }
      return new String[0];
    }

    private boolean isApplicableManifest(Manifest manifest) {
      Attributes attributes = manifest.getMainAttributes();
      return "eforms-notice-viewer".equals(get(attributes, "Implementation-Title"));
    }

    private static Object get(Attributes attributes, String key) {
      return attributes.get(new Attributes.Name(key));
    }
  }
}
