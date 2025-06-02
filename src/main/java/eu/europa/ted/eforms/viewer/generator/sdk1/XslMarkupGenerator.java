package eu.europa.ted.eforms.viewer.generator.sdk1;

import java.io.IOException;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
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
import eu.europa.ted.efx.interfaces.Argument;
import eu.europa.ted.efx.interfaces.MarkupGenerator;
import eu.europa.ted.efx.interfaces.Parameter;
import eu.europa.ted.efx.interfaces.TranslatorOptions;
import eu.europa.ted.efx.model.expressions.Expression;
import eu.europa.ted.efx.model.expressions.path.PathExpression;
import eu.europa.ted.efx.model.expressions.scalar.NumericExpression;
import eu.europa.ted.efx.model.expressions.scalar.StringExpression;
import eu.europa.ted.efx.model.templates.Conditional;
import eu.europa.ted.efx.model.templates.Markup;
import eu.europa.ted.efx.model.types.EfxDataType;

@SdkComponent(versions = {"1", "2"}, componentType = SdkComponentType.MARKUP_GENERATOR)
public class XslMarkupGenerator implements MarkupGenerator {
  private static final Logger logger = LoggerFactory.getLogger(XslMarkupGenerator.class);

  /**
   * Maps {@link EfxDataType} to their corresponding XSL data type.
   */
  static final Map<Class<? extends EfxDataType>, Markup> xsTypeFromEfxDataType = Map
      .ofEntries(
          Map.entry(EfxDataType.String.class, new Markup("xs:string")), //
          Map.entry(EfxDataType.MultilingualString.class, new Markup("xs:string")), //
          Map.entry(EfxDataType.Boolean.class, new Markup("xs:boolean")), //
          Map.entry(EfxDataType.Number.class, new Markup("xs:decimal")), //
          Map.entry(EfxDataType.Date.class, new Markup("xs:date")), //
          Map.entry(EfxDataType.Time.class, new Markup("xs:time")), //
          Map.entry(EfxDataType.Duration.class, new Markup("xs:duration")) //
      );

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
    return this.composeOutputFile(new ArrayList<>(), body, templates);
  }

  @Override
  public Markup composeOutputFile(final List<Markup> globals, final List<Markup> body, final List<Markup> templates) {
    logger.trace("Composing output file with:\n\t- body:\n{}\n\t- templates:\n{}", body, templates);

    final Markup unformattedMarkup = generateMarkup(
        FreemarkerTemplate.OUTPUT_FILE,
        Pair.of("translations", translations),
        Pair.of("globals", markupsListToStringList(globals)),
        Pair.of("body", markupsListToStringList(body)),
        Pair.of("templates", markupsListToStringList(templates)),
        Pair.of("decimalSeparator", translatorOptions.getDecimalFormat().getDecimalSeparator()),
        Pair.of("groupingSeparator", translatorOptions.getDecimalFormat().getGroupingSeparator()),
        Pair.of("udfNamespace", translatorOptions.getUserDefinedFunctionNamespace()));

    try {
      final String formattedScript = XmlHelper.formatXml(unformattedMarkup.script, false);
      return new Markup(formattedScript);
    } catch (DocumentException | IOException e) {
      throw new RuntimeException("Failed to format file output", e);
    }
  }

  @Override
  public Markup renderVariableDeclaration(Class<? extends EfxDataType> type, String name, Expression initialiser) {
    return generateMarkup(
        FreemarkerTemplate.VARIABLE_DECLARATION,
        Pair.of("type", this.getEfxDataTypeEquivalent(type).script),
        Pair.of("name", name),
        Pair.of("initialiser", initialiser.getScript()));
  }

  @Override
  /**
   * Renders a function declaration in the markup.
   *
   * @param type The return type of the function, represented as a class extending {@link EfxDataType}.
   * @param name The name of the function to be declared.
   * @param parameters A map of parameter names to their respective types, represented as classes extending {@link EfxDataType}.
   * @param expression The body of the function, represented as an {@link Expression}.
   * @return A {@link Markup} object containing the rendered function declaration.
   */
  public Markup renderFunctionDeclaration(Class<? extends EfxDataType> type, String name, Map<String, Class<? extends EfxDataType>> parameters, Expression expression) {
    return generateMarkup(
        FreemarkerTemplate.FUNCTION_DECLARATION,
        Pair.of("type", this.getEfxDataTypeEquivalent(type)),
        Pair.of("name", name),
        Pair.of("parameters", parameters.entrySet().stream()
            .map(entry -> Map.of("name", entry.getKey(), "type", this.getEfxDataTypeEquivalent(entry.getValue())))
            .collect(Collectors.toList())),
        Pair.of("expression", expression.getScript()),
        Pair.of("udfNamespace", translatorOptions.getUserDefinedFunctionNamespace()));
  }

  @Override
  public Markup renderVariableExpression(final Expression valueReference) {
    logger.trace("Rendering variable expression [{}]", valueReference);

    return generateMarkup(
        FreemarkerTemplate.VALUE_OF,
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
  public Markup composeFragmentDefinition(String name, String number, Set<Conditional> conditionals,
      Markup content, Markup children, Set<Parameter> parameters) {

    logger.trace("Composing fragment definition with: name={}, number={}, content={}", name, number, content);

    return generateMarkup(
        FreemarkerTemplate.FRAGMENT_DEFINITION,
        Pair.of("conditionals", conditionals.stream()
            .map(conditional -> Map.of(
                "condition", conditional.getCondition().getScript(),
                "content", conditional.getMarkup().script))
            .collect(Collectors.toList())),
        Pair.of("content", content.script),
        Pair.of("children", children.script),
        Pair.of("name", name),
        Pair.of("number", number),
        Pair.of("parameters", parameters));
  }

  @Override
  public Markup renderContextLoop(final PathExpression context, final Markup content,
      final Set<Argument> arguments) {
    logger.trace("Rendering context loop with: context={}", context);

    return generateMarkup(
        FreemarkerTemplate.CONTEXT_LOOP,
        Pair.of("context", context.getScript()),
        Pair.of("variables", arguments),
        Pair.of("content", content.script));
  }

  @Override
  public Markup renderFragmentInvocation(String name, Set<Argument> arguments) {
    logger.trace("Rendering fragment invocation with: name={}", name);

    return generateMarkup(
        FreemarkerTemplate.FRAGMENT_INVOCATION,
        Pair.of("name", name),
        Pair.of("parameters", arguments));
  }

  @Override
  public String escapeSpecialCharacters(String text) {
    if (text == null) {
      return null;
    }

    return text
        // Replace & that are NOT part of existing HTML entities (&#...; or &name;)
        .replaceAll("&(?![#a-zA-Z0-9]+;)", "&#38;")
        .replace("<", "&#60;")
        .replace(">", "&#62;")
        .replace("\"", "&#34;")
        .replace("'", "&#39;");
  }

  @Override
  public Markup getEfxDataTypeEquivalent(Class<? extends EfxDataType> type) {
    return xsTypeFromEfxDataType.getOrDefault(type, Markup.empty());
  }
}
