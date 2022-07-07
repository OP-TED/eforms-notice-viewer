package eu.europa.ted.eforms.viewer.helpers;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SdkConstants {
  public static final String FIELDS_JSON_XML_STRUCTURE_KEY = "xmlStructure";
  public static final String FIELDS_JSON_FIELDS_KEY = "fields";

  public static final String DEFAULT_SDK_ROOT = "eforms-sdk";

  public static final String SDK_GROUP_ID = "eu.europa.ted.eforms";
  public static final String SDK_ARTIFACT_ID = "eforms-sdk";
  public static final String SDK_PACKAGING = "jar";

  public static final Map<String, String> SDK_VERSIONS_MAP;

  static {
    Map<String, String> versionsMap = new HashMap<>();
    versionsMap.put("0.6", "0.6.2");
    versionsMap.put("0.7", "0.7.0-SNAPSHOT");

    SDK_VERSIONS_MAP = Collections.unmodifiableMap(versionsMap);
  }

  private SdkConstants() {}

  public enum ResourceType {
    CODELISTS(Path.of("codelists")), NOTICE_TYPES_VIEW_TEMPLATE(
        Path.of("view-templates")), SDK_FIELD(Path.of("fields")), TRANSLATION(
            Path.of("translations")), SDK_FIELDS_FIELDS_JSON(Path.of("fields", "fields.json"));

    public static final String FIELDS_JSON_XML_STRUCTURE_KEY = "xmlStructure";

    public static final String FIELDS_JSON_FIELDS_KEY = "fields";

    private Path path;

    private ResourceType(Path path) {
      this.path = path;
    }

    public Path getPath() {
      return path;
    }
  }
}
