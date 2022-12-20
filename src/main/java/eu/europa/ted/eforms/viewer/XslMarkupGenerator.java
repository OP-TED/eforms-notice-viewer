package eu.europa.ted.eforms.viewer;

import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.viewer.enums.FreemarkerTemplate;
import eu.europa.ted.eforms.viewer.helpers.FreemarkerHelper;
import eu.europa.ted.eforms.viewer.helpers.XmlHelper;
import eu.europa.ted.efx.interfaces.MarkupGenerator;
import eu.europa.ted.efx.model.Expression;
import eu.europa.ted.efx.model.Expression.PathExpression;
import eu.europa.ted.efx.model.Expression.StringExpression;
import eu.europa.ted.efx.model.Markup;

@SdkComponent(versions = {"0.6", "0.7"}, componentType = SdkComponentType.MARKUP_GENERATOR)
public class XslMarkupGenerator implements MarkupGenerator {
  private static final Logger logger = LoggerFactory.getLogger(XslMarkupGenerator.class);

  private static int variableCounter = 0;

  public XslMarkupGenerator() {}

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

  @SafeVarargs
  private static final Markup generateMarkup(final FreemarkerTemplate template,
      Pair<String, Object>... params) {

    final Supplier<String> xslGenerator = () -> {
      logger.debug("Generating markup using template [{}]", template.getPath());
      logger.trace("Template parameters: [{}]", (Object[]) params);

      final Map<String, Object> model =
          Arrays.asList(Optional.ofNullable(params)
              .orElseGet(Pair::emptyArray))
              .stream()
              .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

      try (StringWriter writer = new StringWriter()) {
        FreemarkerHelper.processTemplate(template.getPath(), model, writer);

        return writer.toString();
      } catch (Exception e) {
        throw new RuntimeException(
            MessageFormat.format("Failed to generate markup using template [{0}]",
                template.getPath()),
            e);
      }
    };

    String xsl = null;

    if (ArrayUtils.isNotEmpty(params)) {
      List<String> keyParts = new ArrayList<>();

      Arrays.stream(params).forEach((Object param) -> {
        if (param instanceof Collection) {
          keyParts.addAll(((Collection<?>) param).stream()
              .map((Object o) -> o instanceof Markup ? ((Markup) o).script : o.toString())
              .collect(Collectors.toList()));
        } else {
          keyParts.add(param.toString());
        }
      });

      xsl = Cache.getString(xslGenerator, keyParts.toArray(String[]::new));
    } else {
      xsl = xslGenerator.get();
    }

    return new Markup(xsl);
  }

  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public Markup composeOutputFile(final List<Markup> body, final List<Markup> templates) {
    logger.trace("Composing output file with:\n\t- body:\n{}\n\t- templates:\n{}", body, templates);

    Markup unformattedMarkup = generateMarkup(
        FreemarkerTemplate.OUTPUT_FILE,
        Pair.of("translations", translations),
        Pair.of("body", markupsListToStringList(body)),
        Pair.of("templates", markupsListToStringList(templates)));

    return new Markup(XmlHelper.formatXml(unformattedMarkup.script, false));
  }

  @Override
  public Markup renderVariableExpression(final Expression valueReference) {
    logger.trace("Rendering variable expression [{}]", valueReference);

    return generateMarkup(
        FreemarkerTemplate.VARIABLE_EXPRESSION,
        Pair.of("expression", valueReference.script));
  }

  @Override
  public Markup renderLabelFromKey(final StringExpression key) {
    logger.trace("Rendering label from key [{}]", key);

    return generateMarkup(FreemarkerTemplate.LABEL_FROM_KEY, Pair.of("key", key.script));
  }

  @Override
  public Markup renderLabelFromExpression(final Expression expression) {
    logger.trace("Rendering label from expression [{}]", expression);

    return generateMarkup(
        FreemarkerTemplate.LABEL_FROM_EXPRESSION,
        Pair.of("expression", expression.script),
        Pair.of("labelSuffix", ++variableCounter));
  }

  @Override
  public Markup renderFreeText(final String freeText) {
    logger.trace("Rendering free text [{}]", freeText);

    return generateMarkup(FreemarkerTemplate.FREE_TEXT, Pair.of("freeText", freeText));
  }

  @Override
  public Markup composeFragmentDefinition(final String name, final String number,
      final Markup content) {
    logger.trace("Composing fragment definition with: name={}, number={}, content={}", name, number,
        content);

    return generateMarkup(
        FreemarkerTemplate.FRAGMENT_DEFINITION,
        Pair.of("content", content.script),
        Pair.of("name", name),
        Pair.of("number", number));
  }

  @Override
  public Markup renderFragmentInvocation(final String name, final PathExpression context) {
    logger.trace("Rendering fragment invocation with: name={}, context={}", name, context.script);

    return generateMarkup(
        FreemarkerTemplate.FRAGMENT_INVOCATION,
        Pair.of("context", context.script),
        Pair.of("name", name));
  }
}
