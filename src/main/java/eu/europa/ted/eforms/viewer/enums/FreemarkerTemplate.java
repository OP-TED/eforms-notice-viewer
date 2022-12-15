package eu.europa.ted.eforms.viewer.enums;

public enum FreemarkerTemplate {
  FRAGMENT_DEFINITION("templates/xsl_markup/fragment_definition.ftl"),
  FRAGMENT_INVOCATION("templates/xsl_markup/fragment_invocation.ftl"),
  FREE_TEXT("templates/xsl_markup/free_text.ftl"),
  LABEL_FROM_EXPRESSION("templates/xsl_markup/label_from_expression.ftl"),
  LABEL_FROM_KEY("templates/xsl_markup/label_from_key.ftl"),
  OUTPUT("templates/xsl_markup/output.ftl"),
  VAR_EXPRESSION("templates/xsl_markup/var_expression.ftl");

  private String path;

  private FreemarkerTemplate(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }
}
