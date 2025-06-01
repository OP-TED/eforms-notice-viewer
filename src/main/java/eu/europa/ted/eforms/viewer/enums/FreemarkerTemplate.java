package eu.europa.ted.eforms.viewer.enums;

/**
 * An enumeration of the available Freemarker templates and the paths used to locate them.
 */
public enum FreemarkerTemplate {
  FRAGMENT_DEFINITION("xsl_markup/fragment_definition.ftl"),
  FRAGMENT_INVOCATION("xsl_markup/fragment_invocation.ftl"),
  CONTEXT_LOOP("xsl_markup/context_loop.ftl"),
  FREE_TEXT("xsl_markup/free_text.ftl"),
  LABEL_FROM_EXPRESSION("xsl_markup/label_from_expression.ftl"),
  LABEL_FROM_KEY("xsl_markup/label_from_key.ftl"),
  OUTPUT_FILE("xsl_markup/output_file.ftl"),
  VALUE_OF("xsl_markup/value_of.ftl"),
  VARIABLE_DECLARATION("xsl_markup/variable_declaration.ftl"),
  FUNCTION_DECLARATION("xsl_markup/function_declaration.ftl");

  private String path;

  private FreemarkerTemplate(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }
}
