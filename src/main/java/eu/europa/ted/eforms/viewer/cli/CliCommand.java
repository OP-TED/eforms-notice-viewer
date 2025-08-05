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
import java.util.Set;
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
import eu.europa.ted.eforms.viewer.enums.ProfilerConfig;
import eu.europa.ted.eforms.viewer.util.xml.TranslationUriResolver;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "", mixinStandardHelpOptions = true, 
    description = {
        "  ▄▖           ▖ ▖  ▗ ▘      ▖▖▘         ",
        "█▌▙▖▛▌▛▘▛▛▌▛▘  ▛▖▌▛▌▜▘▌▛▘█▌  ▌▌▌█▌▌▌▌█▌▛▘",
        "▙▖▌ ▙▌▌ ▌▌▌▄▌  ▌▝▌▙▌▐▖▌▙▖▙▖  ▚▘▌▙▖▚▚▘▙▖▌ ",
        "                                         ",
        "Converts eForms XML notices to HTML using SDK templates and translations.",
        "Automatically downloads required SDK versions from Maven Central.",
        ""
    },
    versionProvider = CliCommand.ManifestVersionProvider.class)
public class CliCommand implements Callable<Integer> {
  private static final Logger logger = LoggerFactory.getLogger(CliCommand.class);

  @Spec
  CommandSpec spec; // injected by picocli

  private String language;

  @Parameters(index = "1", description = "Path to the eForms notice XML file to convert to HTML.")
  private Path noticeXmlPath;

  @Option(names = {"-i", "--viewId"}, description = "Override notice subtype with specific view ID (e.g., 'summary').")
  private String viewId;

  @Option(names = {"-r", "--sdkRoot"}, description = "Directory where eForms SDK versions are stored (default: ~/eforms-sdk).")
  private String sdkResourcesRoot;

  @Option(names = {"-p", "--profile"}, 
      description = "Enable profiling for performance analysis. Options: xslt, efx, all (default: all if no value specified).",
      fallbackValue = "all")
  private String profilerOptions = "";

  @Option(names = {"-f", "--force"},
      description = "Force rebuilding of XSL templates by clearing cached content.")
  private boolean forceBuild;

  @Option(names = {"-s", "--snapshots"},
      description = "Allow downloading SNAPSHOT SDK versions instead of stable releases.")
  private boolean allowSnapshots;

  @Option(names = {"-t", "--templatesRoot"}, description = "Override directory for Freemarker templates (default: './templates').")
  void setTemplatesRoot(String templatesRoot) {
    System.setProperty(NoticeViewerConstants.TEMPLATES_ROOT_DIR_PROPERTY, templatesRoot);
  }

  @Parameters(index = "0", description = "Two letter language code (e.g., 'en', 'fr') for notice display language.")
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
   * Parse the profile options and return the configuration set.
   */
  private Set<ProfilerConfig> getProfilerConfig() {
    try {
      return ProfilerConfig.parseOptions(profilerOptions);
    } catch (IllegalArgumentException e) {
      throw new ParameterException(spec.commandLine(), e.getMessage());
    }
  }

  /**
   * Executes the command line application.
   * 
   * @return Status code (0 for success)
   * @throws IOException If an error occurs during input or output operations
   * @throws ParserConfigurationException If an error occurs in XML parser configuration
   * @throws SAXException If an XML parsing error occurs
   * @throws InstantiationException If an instantiation error occurs
   * @throws URISyntaxException If a URI syntax error occurs
   * @throws TransformerException If an XSLT transformation error occurs
   * @throws XPathExpressionException If an XPath expression error occurs
   */
  @Override
  public Integer call()
      throws IOException, SAXException, ParserConfigurationException, InstantiationException,
      URISyntaxException, TransformerException, XPathExpressionException {
    Validate.notNull(noticeXmlPath, "Undefined notice XML path");
    if (!Files.isRegularFile(noticeXmlPath)) {
      throw new FileNotFoundException(noticeXmlPath.toString());
    }

    logger.info("Starting notice processing for '{}' language", language);
    
    final String xmlContents = Files.readString(noticeXmlPath);

    // Initialise Freemarker templates so that the templates folder will be populated
    NoticeViewerConfig.getFreemarkerConfig();

    final Path sdkRoot = Optional.ofNullable(sdkResourcesRoot)
        .map(Path::of)
        .orElse(NoticeViewerConstants.DEFAULT_SDK_ROOT_DIR);

    NoticeDocument notice = new NoticeDocument(xmlContents);
    logger.info("Parsed notice document, detected SDK version {} and subtype {}", 
        notice.getEformsSdkVersion(), notice.getNoticeSubType());
    
    try {
      final Set<ProfilerConfig> profilerConfig = getProfilerConfig();
      final Path htmlPath =
          NoticeViewer.Builder
              .create()
              .withXsltProfiler(ProfilerConfig.isXsltProfilerEnabled(profilerConfig))
              .withEfxProfiler(ProfilerConfig.isEfxProfilerEnabled(profilerConfig))
              .withAllowSnapshots(allowSnapshots)
              .withUriResolver(new TranslationUriResolver(notice.getEformsSdkVersion(), sdkRoot))
              .build()
              .generateHtmlFile(language, viewId, notice, null, sdkRoot,
                  NoticeViewerConstants.DEFAULT_TRANSLATOR_OPTIONS.getDecimalFormat(),
                  forceBuild);
      
      logger.info("Created HTML file: {}", htmlPath);
    } catch (Exception e) {
      logger.error("Failed to generate HTML file", e);
      throw e;
    }

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
