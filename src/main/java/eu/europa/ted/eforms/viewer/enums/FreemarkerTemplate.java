package eu.europa.ted.eforms.viewer.enums;

public enum FreemarkerTemplate {
  OUTPUT("templates/xsl_markup/output.ftl");

  private String path;

  private FreemarkerTemplate(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }
}
