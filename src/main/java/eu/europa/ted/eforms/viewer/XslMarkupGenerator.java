package eu.europa.ted.eforms.viewer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import eu.europa.ted.efx.interfaces.MarkupGenerator;
import eu.europa.ted.efx.model.Expression;
import eu.europa.ted.efx.model.Expression.PathExpression;
import eu.europa.ted.efx.model.Expression.StringExpression;
import eu.europa.ted.efx.model.Markup;

public class XslMarkupGenerator extends IndentedStringWriter implements MarkupGenerator {
  private static int variableCounter = 0;

  public XslMarkupGenerator() {
    super(10);
  }

  private final String[] assetTypes = {"business_term", "field", "code", "decoration"};

  private final String translations = Arrays.stream(assetTypes).map(assetType -> "fn:document(concat('" + assetType + "_' , $language, '.xml'))").collect(Collectors.joining(", "));

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public Markup composeOutputFile(final List<Markup> body, final List<Markup> templates) {
    // NOTE: you should use a library to build HTML and handle escaping, here we
    // just use String
    // format for demonstration purposes.
    final IndentedStringWriter writer = new IndentedStringWriter(0);
    writer.writeLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    writer.openTag("xsl:stylesheet", "version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" xmlns:efext=\"http://data.europa.eu/p27/eforms-ubl-extensions/1\" xmlns:efac=\"http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1\" xmlns:efbc=\"http://data.europa.eu/p27/eforms-ubl-extension-basic-components/1\" xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\"");
    writer.writeLine("<xsl:output method=\"html\" encoding=\"UTF-8\" indent=\"yes\"/>");
    writer.writeLine("<xsl:param name=\"language\" />");
    writer.writeLine(String.format("<xsl:variable name=\"labels\" select=\"(%s)\"/>", this.translations));
    // Root template.
    writer.openTag("xsl:template", "match=\"/\"");
    writer.openTag("html");
    writer.openTag("head");
    writer.openTag("style");
    writer.writeLine("section { padding: 6px 6px 6px 36px; }");
    writer.writeLine(".text { font-size: 12pt; color: black; }");
    writer.writeLine(".label { font-size: 12pt; color: green; }");
    writer.writeLine(".dynamic-label { font-size: 12pt; color: blue; }");
    writer.writeLine(".value { font-size: 12pt; color: red; }");
    writer.closeTag("style");
    writer.closeTag("head");
    writer.openTag("body");
    writer.writeBlock(body.stream().map(item -> item.script).collect(Collectors.joining("\n")));
    writer.closeTag("body");
    writer.closeTag("html");
    writer.closeTag("xsl:template");
    writer.writeBlock(templates.stream().map(template -> template.script).collect(Collectors.joining("\n")));
    writer.closeTag("xsl:stylesheet");
    return new Markup(writer.toString());
  }

  @Override
  public Markup renderVariableExpression(final Expression valueReference) {
    return new Markup(String.format("<span class=\"value\"><xsl:value-of select=\"%s\"/></span>", valueReference.script));
  }

  @Override
  public Markup renderLabelFromKey(final StringExpression key) {
    return new Markup(String.format("<span class=\"label\"><xsl:value-of " + "select=\"($labels//entry[@key=%s]/text(), concat('{', %s, '}'))[1]\"/></span>", key.script, key.script));
  }

  @Override
  public Markup renderLabelFromExpression(final Expression expression) {
    final IndentedStringWriter writer = new IndentedStringWriter(0);
    final String variableName = String.format("label%d", ++variableCounter);
    writer.writeLine(String.format("<xsl:variable name=\"%s\" select=\"%s\"/>", variableName, expression.script));
    writer.writeLine(String.format("<span class=\"dynamic-label\"><xsl:value-of " + "select=\"($labels//entry[@key=$%s]/text(), concat('{', $%s, '}'))[1]\"/></span>", variableName, variableName));
    return new Markup(writer.toString());
  }

  @Override
  public Markup renderFreeText(final String freeText) {
    return new Markup(String.format("<span class=\"text\"><xsl:text>%s</xsl:text></span>", freeText));
  }

  @Override
  public Markup composeFragmentDefinition(final String name, final String number, final Markup content) {
    final IndentedStringWriter writer = new IndentedStringWriter(0);
    final String tagTemplate = "xsl:template";
    writer.openTag(tagTemplate, String.format("name='%s'", name));
    final String tagSection = "section";
    writer.openTag(String.format(tagSection, "title=\"%s\"", name));
    if (StringUtils.isNotBlank(number)) {
      writer.writeLine(String.format("<xsl:text>%s&#160;</xsl:text>", number));
    }
    writer.writeBlock(content.script);
    writer.closeTag(tagSection);
    writer.closeTag(tagTemplate);
    return new Markup(writer.toString());
  }

  @Override
  public Markup renderFragmentInvocation(final String name, final PathExpression context) {
    final IndentedStringWriter writer = new IndentedStringWriter(0);
    final String tag = "xsl:for-each";
    writer.openTag(tag, String.format("select=\"%s\"", context.script));
    writer.writeBlock(String.format("<xsl:call-template name=\"%s\"/>", name));
    writer.closeTag(tag);
    return new Markup(writer.toString());
  }
}
