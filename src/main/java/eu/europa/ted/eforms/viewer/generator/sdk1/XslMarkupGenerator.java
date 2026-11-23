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
import eu.europa.ted.efx.interfaces.Argument;
import eu.europa.ted.efx.interfaces.MarkupGenerator;
import eu.europa.ted.efx.interfaces.Parameter;
import eu.europa.ted.efx.interfaces.TranslatorContext;
import eu.europa.ted.efx.interfaces.TranslatorOptions;
import eu.europa.ted.efx.model.expressions.Expression;
import eu.europa.ted.efx.model.expressions.PathExpression;
import eu.europa.ted.efx.model.expressions.TypedExpression;
import eu.europa.ted.efx.model.expressions.scalar.NumericExpression;
import eu.europa.ted.efx.model.expressions.scalar.StringExpression;
import eu.europa.ted.efx.model.templates.Conditional;
import eu.europa.ted.efx.model.templates.FragmentKind;
import eu.europa.ted.efx.model.templates.FragmentOutline;
import eu.europa.ted.efx.model.templates.Markup;
import eu.europa.ted.efx.model.templates.OutlineOffset;
import eu.europa.ted.efx.model.types.EfxDataType;
import eu.europa.ted.efx.model.types.EfxTypeLattice;

@SdkComponent(versions = {"1"}, componentType = SdkComponentType.MARKUP_GENERATOR)
public class XslMarkupGenerator implements MarkupGenerator {
  private static final Logger logger = LoggerFactory.getLogger(XslMarkupGenerator.class);

  /**
   * Maps primitive {@link EfxDataType} to their corresponding XSL data type.
   */
  static final Map<Class<? extends EfxDataType.Primitive>, Markup> xsTypeFromEfxDataType = Map
      .ofEntries(
          Map.entry(EfxDataType.String.class, new Markup("xs:string")), //
          Map.entry(EfxDataType.MultilingualString.class, new Markup("xs:string")), //
          Map.entry(EfxDataType.Boolean.class, new Markup("xs:boolean")), //
          Map.entry(EfxDataType.Number.class, new Markup("xs:decimal")), //
          Map.entry(EfxDataType.Date.class, new Markup("xs:date")), //
          Map.entry(EfxDataType.Time.class, new Markup("xs:time")), //
          Map.entry(EfxDataType.Duration.class, new Markup("xs:duration")), //
          Map.entry(EfxDataType.Node.class, new Markup("node()")) //
      );

  protected static int variableCounter = 0;

  protected TranslatorOptions translatorOptions;

  public XslMarkupGenerator(TranslatorOptions translatorOptions) {
    this.translatorOptions = Validate.notNull(translatorOptions, "Undefined translator options");
  }

  protected String[] getAssetTypes() {
    return new String[] {"business-term", "field", "code", "auxiliary"};
  }

  protected final String translations = "(" + Arrays.stream(getAssetTypes())
      .map(assetType -> "fn:document(concat('" + assetType + "_' , $LANGUAGE, '.xml'))")
      .collect(Collectors.joining(", ")) + ")";

  protected static List<String> markupsListToStringList(List<Markup> markupsList) {
    return Optional.ofNullable(markupsList).orElse(Collections.emptyList()).stream()
        .map((Markup markup) -> markup.script).collect(Collectors.toList());
  }

  @SafeVarargs
  protected static final Markup generateMarkup(final FreemarkerTemplate template,
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
      logger.error("Failed to generate markup using template [{}]. Cause: {}", 
          template.getPath(), e.getMessage(), e);
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
  public Markup composeOutputFile(final List<Markup> globals, final List<Markup> body, final List<Markup> summary, final List<Markup> navigation, final List<Markup> fragments) {
    logger.trace("Composing output file with:\n\t- body:\n{}\n\t- templates:\n{}", body, fragments);

    final Markup unformattedMarkup = generateMarkup(
        FreemarkerTemplate.OUTPUT_FILE,
        Pair.of("translations", translations),
        Pair.of("globals", markupsListToStringList(globals)),
        Pair.of("body", markupsListToStringList(body)),
        Pair.of("summary", markupsListToStringList(summary)),
        Pair.of("navigation", markupsListToStringList(navigation)),
        Pair.of("templates", markupsListToStringList(fragments)),
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
  public Markup renderVariableDeclaration(String name, TypedExpression initialiser) {
    return generateMarkup(
        FreemarkerTemplate.VARIABLE_DECLARATION,
        Pair.of("type", this.getEfxDataTypeEquivalent(EfxTypeLattice.toPrimitive(initialiser.getDataType())).script),
        Pair.of("name", name),
        Pair.of("initialiser", initialiser.getScript()));
  }

  @Override
  /**
   * Renders a function declaration in the markup.
   *
   * @param name The name of the function to be declared.
   * @param parameters A map of parameter names to their respective types, represented as classes extending {@link EfxDataType}.
   * @param expression The body of the function, represented as a {@link TypedExpression}.
   * @return A {@link Markup} object containing the rendered function declaration.
   */
  public Markup renderFunctionDeclaration(String name, Map<String, Class<? extends EfxDataType>> parameters, TypedExpression expression) {
    return generateMarkup(
        FreemarkerTemplate.FUNCTION_DECLARATION,
        Pair.of("type", this.getEfxDataTypeEquivalent(EfxTypeLattice.toPrimitive(expression.getDataType())).script),
        Pair.of("name", name),
        Pair.of("parameters", parameters.entrySet().stream()
            .map(entry -> Map.of("name", entry.getKey(), "type", this.getEfxDataTypeEquivalent(EfxTypeLattice.toPrimitive(entry.getValue())).script))
            .collect(Collectors.toList())),
        Pair.of("expression", expression.getScript()),
        Pair.of("udfNamespace", translatorOptions.getUserDefinedFunctionNamespace()));
  }

  @Override
  public Markup renderVariableExpression(final Expression valueReference, TranslatorContext translatorContext) {
    logger.trace("Rendering variable expression [{}]", valueReference);

    return generateMarkup(
        FreemarkerTemplate.VALUE_OF,
        Pair.of("expression", valueReference.getScript()));
  }

  @Override
  public Markup renderLabelFromKey(final StringExpression key, TranslatorContext translatorContext) {
    return this.renderLabelFromKey(key, NumericExpression.empty(), translatorContext);
  }

  @Override
  public Markup renderLabelFromKey(final StringExpression key, NumericExpression quantity, TranslatorContext translatorContext) {
    logger.trace("Rendering label from key [{}]", key);

    return generateMarkup(FreemarkerTemplate.LABEL_FROM_KEY, 
      Pair.of("key", key.getScript()),
      Pair.of("quantity", quantity.getScript()));
  }

  @Override
  public Markup renderLabelFromExpression(final Expression expression, TranslatorContext translatorContext) {
    return this.renderLabelFromExpression(expression, NumericExpression.empty(), translatorContext);
  }

  @Override
  public Markup renderLabelFromExpression(final Expression expression, NumericExpression quantity, TranslatorContext translatorContext) {
    logger.trace("Rendering label from expression [{}]", expression);

    return generateMarkup(
        FreemarkerTemplate.LABEL_FROM_EXPRESSION,
        Pair.of("expression", expression.getScript()),
        Pair.of("variableSuffix", String.valueOf(++variableCounter)),
        Pair.of("quantity", quantity.getScript()));
  }

  @Override
  public Markup renderFreeText(final String freeText, TranslatorContext translatorContext) {
    logger.trace("Rendering free text [{}]", freeText);
    
    return generateMarkup(FreemarkerTemplate.FREE_TEXT,
        Pair.of("freeText", freeText.replace(" ", "&#8200;")));
  }

  @Override
  public Markup renderHyperlink(Markup label, StringExpression url, TranslatorContext translatorContext) {
    logger.trace("Rendering hyperlink with label [{}] and URL [{}]", label, url);

    return generateMarkup(
        FreemarkerTemplate.HYPERLINK,
        Pair.of("label", label.script),
        Pair.of("url", url.getScript()));
  }

  @Override
  public Markup renderLineBreak(TranslatorContext translatorContext) {
    return new Markup("<br/>");
  }

  /**
   * Composes a fragment definition. The kind, the nesting level and the heading level of the
   * fragment are passed to the template as {@code style} (see {@link #styleOf(FragmentKind)};
   * "normal" for an EFX-1 line), {@code nestingLevel} and {@code headingLevel} (0 for an EFX-1 line).
   * An EFX-1 line has no navigation id body: a numbered one is identified by its outline number.
   */
  @Override
  public Markup composeFragmentDefinition(String name, FragmentOutline outline,
      List<Conditional> conditionals, Markup content, Markup children, Set<Parameter> parameters,
      TranslatorContext translatorContext) {
    final String number = outline.getNumber();
    return this.composeFragmentDefinition(name, number,
        number.isEmpty() ? "" : "section" + number.replace('.', '_'), outline.getKind(),
        outline.getNestingLevel(), outline.getHeadingLevel(), conditionals, content, children,
        parameters);
  }

  /**
   * Renders a fragment definition. A fragment that is not displayed itself gets no style, so the
   * template writes no section for it.
   *
   * @param id the value of the id attribute of the section, ready to be written out, or "" for no id
   * @param kind what the fragment is; passed to the template as {@code style} (see
   *        {@link #styleOf(FragmentKind)})
   * @param nestingLevel the nesting level of the line where it is displayed, ready to be written
   *        out; passed to the template as {@code nestingLevel}
   * @param headingLevel the heading level of the line where it is displayed, ready to be written
   *        out; passed to the template as {@code headingLevel}
   */
  protected Markup composeFragmentDefinition(String name, String number, String id,
      FragmentKind kind, Object nestingLevel, Object headingLevel, List<Conditional> conditionals,
      Markup content, Markup children, Set<Parameter> parameters) {

    logger.trace(
        "Composing fragment definition with: name={}, number={}, id={}, kind={}, nestingLevel={}, headingLevel={}, content={}",
        name, number, id, kind, nestingLevel, headingLevel, content);

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
        Pair.of("id", id),
        Pair.of("style", styleOf(kind)),
        Pair.of("nestingLevel", nestingLevel),
        Pair.of("headingLevel", headingLevel),
        Pair.of("parameters", parameters));
  }

  /**
   * @return how a fragment of the given kind is displayed, as the template receives it: "heading"
   *         or "normal" for a template line that is displayed in a section of its own, and "" for a
   *         fragment that is not displayed itself (a line that invokes a template, whose templates
   *         take its place, or a template that holds lines, whose lines take the place of the line
   *         that invokes it), for which the template writes no section
   */
  protected static String styleOf(final FragmentKind kind) {
    switch (kind) {
      case HEADING:
        return "heading";
      case NORMAL:
        return "normal";
      default:
        return "";
    }
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

  /**
   * Renders the call of a fragment. EFX-1 has no navigation ids, and its levels are not used, so
   * what the call puts above the fragment is not used.
   */
  @Override
  public Markup renderFragmentInvocation(String name, OutlineOffset offset,
      Set<Argument> arguments, TranslatorContext translatorContext) {
    return this.renderCall(name, arguments);
  }

  /**
   * Renders the call of a fragment with the given arguments.
   */
  protected Markup renderCall(String name, Set<Argument> arguments) {
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

  @Override
  public Markup renderDictionaryDeclaration(String name, PathExpression match,
      StringExpression key) {
    logger.trace("Rendering dictionary declaration with: name={}", name);

    return generateMarkup(
        FreemarkerTemplate.DICTIONARY_DECLARATION,
        Pair.of("name", name),
        Pair.of("match", match.getScript()),
        Pair.of("key", key.getScript()));
  }
}
