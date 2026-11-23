package eu.europa.ted.eforms.viewer.enums;

/**
 * An enumeration of the available Freemarker templates and the paths used to locate them.
 */
public enum FreemarkerTemplate {
  FRAGMENT_DEFINITION("xsl-markup/html/fragment_definition.ftl"),
  FRAGMENT_INVOCATION("xsl-markup/html/fragment_invocation.ftl"),
  CONTEXT_LOOP("xsl-markup/html/context_loop.ftl"),
  FREE_TEXT("xsl-markup/html/free_text.ftl"),
  LABEL_FROM_EXPRESSION("xsl-markup/html/label_from_expression.ftl"),
  LABEL_FROM_KEY("xsl-markup/html/label_from_key.ftl"),
  OUTPUT_FILE("xsl-markup/html/output_file.ftl"),
  HYPERLINK("xsl-markup/html/hyperlink.ftl"),
  VALUE_OF("xsl-markup/html/value_of.ftl"),
  VARIABLE_DECLARATION("xsl-markup/html/variable_declaration.ftl"),
  DICTIONARY_DECLARATION("xsl-markup/html/dictionary_declaration.ftl"),
  FUNCTION_DECLARATION("xsl-markup/html/function_declaration.ftl"),
  // JSON templates
  OUTPUT_FILE_JSON("xsl-markup/json/output_file.ftl");

  private String path;

  private FreemarkerTemplate(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }
}
