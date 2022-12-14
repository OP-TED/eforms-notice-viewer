package eu.europa.ted.eforms.viewer;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.viewer.enums.FreemarkerTemplate;
import eu.europa.ted.eforms.viewer.helpers.FreemarkerHelper;
import eu.europa.ted.eforms.viewer.helpers.IndentedStringWriter;
import eu.europa.ted.efx.interfaces.MarkupGenerator;
import eu.europa.ted.efx.model.Expression;
import eu.europa.ted.efx.model.Expression.PathExpression;
import eu.europa.ted.efx.model.Expression.StringExpression;
import eu.europa.ted.efx.model.Markup;
import freemarker.template.TemplateException;

@SdkComponent(versions = {"0.6", "0.7"}, componentType = SdkComponentType.MARKUP_GENERATOR)
public class XslMarkupGenerator extends IndentedStringWriter implements MarkupGenerator {
  private static final Logger logger = LoggerFactory.getLogger(XslMarkupGenerator.class);

  private static int variableCounter = 0;

  public XslMarkupGenerator() {
    super(10);
  }

  protected String[] getAssetTypes() {
    return new String[] {"business_term", "field", "code", "decoration"};
  }

  private final String translations = Arrays.stream(getAssetTypes())
      .map(assetType -> "fn:document(concat('" + assetType + "_' , $language, '.xml'))")
      .collect(Collectors.joining(", "));

  private static List<String> markupsListToStringList(List<Markup> markupsList) {
    return Optional.ofNullable(markupsList).orElse(Collections.emptyList()).stream()
        .map((Markup markup) -> markup.script).collect(Collectors.toList());
  }

  private static Markup generateMarkup(final Map<String, Object> model,
      final FreemarkerTemplate template) {
    logger.debug("Generating markup using template [{}]", template.getPath());

    try (StringWriter writer = new StringWriter()) {
      FreemarkerHelper.processTemplate(template.getPath(),
          Optional.ofNullable(model).orElseGet(HashMap::new), writer);

      return new Markup(writer.toString());
    } catch (IOException | TemplateException e) {
      throw new RuntimeException(
          MessageFormat.format("Failed to generate markup using template [{0}]",
              template.getPath()),
          e);
    }
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public Markup composeOutputFile(final List<Markup> body, final List<Markup> templates) {
    final Map<String, Object> model = new HashMap<>();

    model.put("translations", this.translations);
    model.put("body", markupsListToStringList(body));
    model.put("templates", markupsListToStringList(templates));

    return generateMarkup(model, FreemarkerTemplate.OUTPUT);
  }

  @Override
  public Markup renderVariableExpression(final Expression valueReference) {
    return new Markup(String.format("<span class=\"value\"><xsl:value-of select=\"%s\"/></span>",
        valueReference.script));
  }

  @Override
  public Markup renderLabelFromKey(final StringExpression key) {
    return new Markup(String.format(
        "<span class=\"label\"><xsl:value-of "
            + "select=\"($labels//entry[@key=%s]/text(), concat('{', %s, '}'))[1]\"/></span>",
        key.script, key.script));
  }

  @Override
  public Markup renderLabelFromExpression(final Expression expression) {
    final IndentedStringWriter writer = new IndentedStringWriter(0);
    final String outerVariableName = String.format("labels%d", ++variableCounter);
    final String innerVariableName = String.format("label%d", variableCounter);
    writer.writeLine("");
    writer.openTag("span", "class=\"dynamic-label\"");
    writer.openTag("xsl:variable",
        String.format("name=\"%s\" as=\"xs:string*\"", outerVariableName));
    writer.openTag("xsl:for-each", String.format("select=\"%s\"", expression.script));
    writer.writeLine(String.format("<xsl:variable name=\"%s\" select=\".\"/>", innerVariableName));
    writer.writeLine(String.format(
        "<xsl:value-of select=\"($labels//entry[@key=$%s]/text(), concat('{', $%s, '}'))[1]\"/>",
        innerVariableName, innerVariableName));
    writer.closeTag("xsl:for-each");
    writer.closeTag("xsl:variable");
    writer.writeLine(
        String.format("<xsl:value-of select=\"string-join($%s, ', ')\"/>", outerVariableName));
    writer.closeTag("span");
    return new Markup(writer.toString());
  }

  @Override
  public Markup renderFreeText(final String freeText) {
    return new Markup(
        String.format("<span class=\"text\"><xsl:text>%s</xsl:text></span>", freeText));
  }

  @Override
  public Markup composeFragmentDefinition(final String name, final String number,
      final Markup content) {
    logger.trace("Composing fragment definition with: name={}, number={}", name, number);

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
    logger.trace("Rendering fragment invocation with: name={}, context={}", name, context.script);

    final IndentedStringWriter writer = new IndentedStringWriter(0);
    final String tag = "xsl:for-each";
    writer.openTag(tag, String.format("select=\"%s\"", context.script));
    writer.writeBlock(String.format("<xsl:call-template name=\"%s\"/>", name));
    writer.closeTag(tag);
    return new Markup(writer.toString());
  }
}
