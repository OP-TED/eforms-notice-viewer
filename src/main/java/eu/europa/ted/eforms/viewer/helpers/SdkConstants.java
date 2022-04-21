package eu.europa.ted.eforms.viewer.helpers;

import java.nio.file.Path;

public class SdkConstants {
  public static final Path EFORMS_SDK_CODELISTS = Path.of("eforms-sdk", "codelists");

  public static final Path EFORMS_SDK_EXAMPLES_NOTICES =
      Path.of("eforms-sdk", "examples", "notices");

  public static final Path EFORMS_SDK_NOTICE_TYPES_VIEW_TEMPLATES =
      Path.of("eforms-sdk", "notice-types", "view-templates");

  public static final Path EFORMS_SDK_FIELDS_FIELDS_JSON =
      Path.of("eforms-sdk", "fields", "fields.json");

  public static final String FIELDS_JSON_XML_STRUCTURE_KEY = "xmlStructure";
  public static final String FIELDS_JSON_FIELDS_KEY = "fields";
}
