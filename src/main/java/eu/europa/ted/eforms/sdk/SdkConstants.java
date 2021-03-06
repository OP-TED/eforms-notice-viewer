package eu.europa.ted.eforms.sdk;

import java.nio.file.Path;

public class SdkConstants {
  public static final String FIELDS_JSON_XML_STRUCTURE_KEY = "xmlStructure";
  public static final String FIELDS_JSON_FIELDS_KEY = "fields";

  public static final String DEFAULT_SDK_ROOT = "eforms-sdk";

  public static final String SDK_GROUP_ID = "eu.europa.ted.eforms";
  public static final String SDK_ARTIFACT_ID = "eforms-sdk";
  public static final String SDK_PACKAGING = "jar";

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
