package eu.europa.ted.eforms.viewer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import eu.europa.ted.eforms.viewer.helpers.ResourceLoader;

/**
 * @deprecated The class was used in an earlier incarnation of the eForms Viewer. It will be removed before the next release.
 */
@Deprecated
public class SdkLabelMap {

  /**
   * Path relative to maven src/main/resources/
   */
  private static final String SDK_TRANSLATIONS_FOLDER = "eforms-sdk/translations/";

  /**
   * Extension used for SDK translation files.
   */
  private static final String SDK_TRANSLATIONS_EXT = ".xml";

  /**
   * It is implemented as a "kind-of" singleton. One instance per version of the eForms SDK.
   */
  private static final Map<String, SdkLabelMap> instances = new HashMap<>();

  /**
   * Gets the single instance containing the labels defined in the given version of the eForms SDK.
   *
   * @param sdkVersion Version of the SDK
   */
  public static SdkLabelMap getInstance(final String sdkVersion) {
    return instances.computeIfAbsent(sdkVersion, k -> new SdkLabelMap(sdkVersion));
  }

  private final Map<String, Map<String, String>> labelsByLanguage = new HashMap<>();

  /**
   * Protected, use getInstance method instead.
   *
   * @param sdkVersion The version of the SDK to get the labels from.
   */
  protected SdkLabelMap(final String sdkVersion) {
    //
  }

  public String mapLabel(final String labelId, final String language) throws IOException {

    final Map<String, String> labelById =
        labelsByLanguage.computeIfAbsent(language, k -> new HashMap<>());

    if (labelById.isEmpty()) {

      // Lazily load all labels associated to given language.
      listFilesUsingFileWalk(ResourceLoader.getResourceAsPath(SDK_TRANSLATIONS_FOLDER), 2,
          SDK_TRANSLATIONS_EXT).forEach(path -> {
            final String filenameStr = path.getFileName().toString();

            // Rely on SDK naming convention to find the language in the filename.
            final int lastIndexOfLangSeparator = filenameStr.lastIndexOf("_");
            assert lastIndexOfLangSeparator > 0 : "lastIndexOfLangSeparator must be > 0";

            final int lastIndexOfExt = filenameStr.lastIndexOf(SDK_TRANSLATIONS_EXT);
            assert lastIndexOfExt > lastIndexOfLangSeparator : "lastIndexOfExt come after lastIndexOfLangSeparator";

            final String langFromFilename =
                filenameStr.substring(lastIndexOfLangSeparator + 1, lastIndexOfExt);

            // Load data from the file if it contains the desired language.
            if (language.equals(langFromFilename)) {
              populateMap(labelById, path);
            }
          });

      if (labelById.isEmpty()) {
        // It should not be empty anymore after being populated!
        throw new IllegalArgumentException(String.format(
            "Found no texts for language '%s'. Please use a language provided in folder '%s'",
            language, SDK_TRANSLATIONS_FOLDER));
      }
    }

    // Get the label by the SDK unique label identifier.
    final String labelText = labelById.get(labelId);

    final String fallbackLanguage = "en";
    if (labelText != null) {
      return labelText;
    }
    if (language != fallbackLanguage) {
      return mapLabel(labelId, "en"); // Fallback to english.
    }

    return null;
  }

  public String mapLabel(final String labelId, final Locale locale) throws IOException {
    return mapLabel(labelId, locale.getLanguage());
  }

  public String mapLabel(String assetType, String labelType, String assetId, String language)
      throws IOException {
    return this.mapLabel(String.format("%s|%s|%s", assetType, labelType, assetId), language);
  }

  private static void populateMap(final Map<String, String> labelById, Path path) {
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {

      dbf.setValidating(false);
      dbf.setNamespaceAware(true);
      dbf.setFeature("http://xml.org/sax/features/namespaces", false);
      dbf.setFeature("http://xml.org/sax/features/validation", false);
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
      dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

      final DocumentBuilder db = dbf.newDocumentBuilder();
      final Document doc = db.parse(path.toFile());

      // For each entry add the key and the value to the map.
      final NodeList elements = doc.getElementsByTagName("entry");
      for (int i = 0; i < elements.getLength(); i++) {
        final Node item = elements.item(i);
        final String labelId = item.getAttributes().getNamedItem("key").getNodeValue();
        final String labelText = item.getTextContent();
        assert StringUtils.isNotBlank(labelId) : "labelId is blank";
        labelById.put(labelId, labelText);
      }
    } catch (final ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (final SAXException e) {
      throw new RuntimeException(e);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param pathFolder Folder to crawl
   * @param maxDepth Maximum folder depth, minimum is 1 level
   * @param dotExtension Something like .xlsx, or .xml, ... usually to exclude .md or other
   *        irrelevant files. Ends with is performed using this string
   *
   * @return list of paths (files only) ending with the passed the extension.
   */
  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
      value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "False positive.")
  private static List<Path> listFilesUsingFileWalk(final Path pathFolder, final int maxDepth,
      final String dotExtension) throws IOException {
    assert maxDepth > 0 : "maxDepth should be > 0";

    if (!pathFolder.toFile().isDirectory()) {
      throw new RuntimeException(String.format("Expecting folder but got: %s", pathFolder));
    }
    // Works only if name is unique ...
    // final Path rootPath = Paths.get(ClassLoader.getSystemResource(dir).toURI());
    try (Stream<Path> stream = Files.walk(pathFolder, maxDepth)) {
      return stream
          .filter(pathFile -> !Files.isDirectory(pathFile)
              && pathFile.toString().endsWith(dotExtension))
          .sorted()// Alphanumeric sort.
          .collect(Collectors.toList());
    }
  }

}
