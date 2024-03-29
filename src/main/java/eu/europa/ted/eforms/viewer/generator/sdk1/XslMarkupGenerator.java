package eu.europa.ted.eforms.viewer.generator.sdk1;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.viewer.enums.FreemarkerTemplate;
import eu.europa.ted.eforms.viewer.util.FreemarkerHelper;
import eu.europa.ted.eforms.viewer.util.xml.XmlHelper;
import eu.europa.ted.efx.interfaces.MarkupGenerator;
import eu.europa.ted.efx.interfaces.TranslatorOptions;
import eu.europa.ted.efx.model.expressions.Expression;
import eu.europa.ted.efx.model.expressions.path.PathExpression;
import eu.europa.ted.efx.model.expressions.scalar.NumericExpression;
import eu.europa.ted.efx.model.expressions.scalar.StringExpression;
import eu.europa.ted.efx.model.templates.Markup;

@SdkComponent(versions = {"1", "2"}, componentType = SdkComponentType.MARKUP_GENERATOR)
public class XslMarkupGenerator implements MarkupGenerator {
  private static final Logger logger = LoggerFactory.getLogger(XslMarkupGenerator.class);

  private static int variableCounter = 0;

  private TranslatorOptions translatorOptions;

  public XslMarkupGenerator(TranslatorOptions translatorOptions) {
    this.translatorOptions = Validate.notNull(translatorOptions, "Undefined translator options");
  }

  protected String[] getAssetTypes() {
    return new String[] {"business-term", "field", "code", "auxiliary"};
  }

  private final String translations = "(" + Arrays.stream(getAssetTypes())
      .map(assetType -> "fn:document(concat('" + assetType + "_' , $LANGUAGE, '.xml'))")
      .collect(Collectors.joining(", ")) + ")";

  private static List<String> markupsListToStringList(List<Markup> markupsList) {
    return Optional.ofNullable(markupsList).orElse(Collections.emptyList()).stream()
        .map((Markup markup) -> markup.script).collect(Collectors.toList());
  }

  @SafeVarargs
  private static final Markup generateMarkup(final FreemarkerTemplate template,
      Pair<String, Object>... params) {

    logger.trace("Generating markup using template [{}] with parameters: {}", template.getPath(),
        params);

    final Map<String, Object> model =
        Arrays.asList(Optional.ofNullable(params)
            .orElseGet(Pair::emptyArray))
            .stream()
            .filter(pair -> pair.getKey() != null && pair.getValue() != null)
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

    try (StringWriter writer = new StringWriter()) {
      FreemarkerHelper.processTemplate(template.getPath(), model, writer);

      return new Markup(writer.toString());
    } catch (Exception e) {
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
    logger.trace("Composing output file with:\n\t- body:\n{}\n\t- templates:\n{}", body, templates);

    final Markup unformattedMarkup = generateMarkup(
        FreemarkerTemplate.OUTPUT_FILE,
        Pair.of("translations", translations),
        Pair.of("body", markupsListToStringList(body)),
        Pair.of("templates", markupsListToStringList(templates)),
        Pair.of("decimalSeparator", translatorOptions.getDecimalFormat().getDecimalSeparator()),
        Pair.of("groupingSeparator", translatorOptions.getDecimalFormat().getGroupingSeparator()));

    try {
      final String formattedScript = XmlHelper.formatXml(unformattedMarkup.script, false);
      return new Markup(formattedScript);
    } catch (DocumentException | IOException e) {
      throw new RuntimeException("Failed to format file output", e);
    }
  }

  @Override
  public Markup renderVariableExpression(final Expression valueReference) {
    logger.trace("Rendering variable expression [{}]", valueReference);

    return generateMarkup(
        FreemarkerTemplate.VARIABLE_EXPRESSION,
        Pair.of("expression", valueReference.getScript()));
  }

  @Override
  public Markup renderLabelFromKey(final StringExpression key) {
    return this.renderLabelFromKey(key, NumericExpression.empty());
  }

  @Override
  public Markup renderLabelFromKey(final StringExpression key, NumericExpression quantity) {
    logger.trace("Rendering label from key [{}]", key);

    return generateMarkup(FreemarkerTemplate.LABEL_FROM_KEY, 
      Pair.of("key", key.getScript()),
      Pair.of("quantity", quantity.getScript()));
  }

  @Override
  public Markup renderLabelFromExpression(final Expression expression) {
    return this.renderLabelFromExpression(expression, NumericExpression.empty());
  }

  @Override
  public Markup renderLabelFromExpression(final Expression expression, NumericExpression quantity) {
    logger.trace("Rendering label from expression [{}]", expression);

    return generateMarkup(
        FreemarkerTemplate.LABEL_FROM_EXPRESSION,
        Pair.of("expression", expression.getScript()),
        Pair.of("variableSuffix", String.valueOf(++variableCounter)),
        Pair.of("quantity", quantity.getScript()));
  }

  @Override
  public Markup renderFreeText(final String freeText) {
    logger.trace("Rendering free text [{}]", freeText);

    return generateMarkup(FreemarkerTemplate.FREE_TEXT,
        Pair.of("freeText", freeText.replace(" ", "&#8200;")));
  }

  @Override
  public Markup renderLineBreak() {
    return new Markup("<br/>");
  }

  @Override
  public Markup composeFragmentDefinition(String name, String number, Markup content, Set<String> parameters) {
    logger.trace("Composing fragment definition with: name={}, number={}, content={}", name, number, content);

    return generateMarkup(
        FreemarkerTemplate.FRAGMENT_DEFINITION,
        Pair.of("content", content.script),
        Pair.of("name", name),
        Pair.of("number", number),
        Pair.of("parameters", parameters));
  }

  @Override
  public Markup renderFragmentInvocation(final String name, final PathExpression context, final Set<Pair<String, String>> variables) {
    logger.trace("Rendering fragment invocation with: name={}, context={}", name, context.getScript());

    return generateMarkup(
        FreemarkerTemplate.FRAGMENT_INVOCATION,
        Pair.of("context", context.getScript()),
        Pair.of("name", name),
        Pair.of("variables", variables.stream().map(variable -> new String[] { variable.getKey(), variable.getValue() }).toArray())
        );
  }
}
