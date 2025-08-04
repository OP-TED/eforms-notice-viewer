package eu.europa.ted.eforms.viewer.generator.sdk2;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.viewer.enums.FreemarkerTemplate;
import eu.europa.ted.eforms.viewer.generator.sdk1.XslMarkupGenerator;
import eu.europa.ted.eforms.viewer.util.xml.XmlHelper;
import eu.europa.ted.efx.interfaces.Argument;
import eu.europa.ted.efx.interfaces.Parameter;
import eu.europa.ted.efx.interfaces.TemplateSection;
import eu.europa.ted.efx.interfaces.TranslatorContext;
import eu.europa.ted.efx.interfaces.TranslatorOptions;
import eu.europa.ted.efx.model.expressions.Expression;
import eu.europa.ted.efx.model.expressions.scalar.NumericExpression;
import eu.europa.ted.efx.model.expressions.scalar.StringExpression;
import eu.europa.ted.efx.model.templates.Conditional;
import eu.europa.ted.efx.model.templates.Markup;
@SdkComponent(versions = {"2"}, componentType = SdkComponentType.MARKUP_GENERATOR, qualifier = "json")
public class XslMarkupGeneratorForJson extends XslMarkupGenerator {
  private static final Logger logger = LoggerFactory.getLogger(XslMarkupGeneratorForJson.class);


  public XslMarkupGeneratorForJson(TranslatorOptions translatorOptions) {
    super(translatorOptions);
  }


  @Override
  public Markup composeOutputFile(final List<Markup> globals, final List<Markup> main, final List<Markup> summary, final List<Markup> navigation, final List<Markup> fragments) {
    logger.trace("Composing output file with:\n\t- body:\n{}\n\t- templates:\n{}", main, fragments);

    final Markup unformattedMarkup = generateMarkup(
        FreemarkerTemplate.OUTPUT_FILE_JSON,
        Pair.of("translations", translations),
        Pair.of("globals", markupsListToStringList(globals)),
        Pair.of("body", markupsListToStringList(main)),
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
  public Markup renderHyperlink(Markup label, StringExpression url, TranslatorContext translatorContext) {
    if (translatorContext.getCurrentSection() != TemplateSection.NAVIGATION) {
      return super.renderHyperlink(label, url, translatorContext);
    }

    logger.trace("Rendering hyperlink with label [{}] and URL [{}]", label, url);

    return generateMarkup(
        FreemarkerTemplate.HYPERLINK_JSON,
        Pair.of("label", label.script),
        Pair.of("url", url.getScript()));
  }

    @Override
  public Markup renderVariableExpression(final Expression valueReference, TranslatorContext translatorContext) {
    if (translatorContext.getCurrentSection() != TemplateSection.NAVIGATION) {
      return super.renderVariableExpression(valueReference, translatorContext);
    }

    logger.trace("Rendering variable expression [{}]", valueReference);

    return generateMarkup(
        FreemarkerTemplate.VALUE_OF_JSON,
        Pair.of("expression", valueReference.getScript()));
  }

    @Override
  public Markup renderLabelFromKey(final StringExpression key, NumericExpression quantity, TranslatorContext translatorContext) {
    if (translatorContext.getCurrentSection() != TemplateSection.NAVIGATION) {
      return super.renderLabelFromKey(key, quantity, translatorContext);
    }

    logger.trace("Rendering label from key [{}]", key);

    return generateMarkup(FreemarkerTemplate.LABEL_FROM_KEY_JSON, 
      Pair.of("key", key.getScript()),
      Pair.of("quantity", quantity.getScript()));
  }


  @Override
  public Markup renderLabelFromExpression(final Expression expression, NumericExpression quantity, TranslatorContext translatorContext) {
    if (translatorContext.getCurrentSection() != TemplateSection.NAVIGATION) {
      return super.renderLabelFromExpression(expression, quantity, translatorContext);
    }
    logger.trace("Rendering label from expression [{}]", expression);

    return generateMarkup(
        FreemarkerTemplate.LABEL_FROM_EXPRESSION_JSON,
        Pair.of("expression", expression.getScript()),
        Pair.of("variableSuffix", String.valueOf(++variableCounter)),
        Pair.of("quantity", quantity.getScript()));
  }

  @Override
  public Markup renderFreeText(final String freeText, TranslatorContext translatorContext) {
    if (translatorContext.getCurrentSection() != TemplateSection.NAVIGATION) {
      return super.renderFreeText(freeText, translatorContext);
    }
    logger.trace("Rendering free text [{}]", freeText);
    
    return generateMarkup(FreemarkerTemplate.FREE_TEXT_JSON,
        Pair.of("freeText", freeText));
  }

  @Override
  public Markup renderLineBreak(TranslatorContext translatorContext) {
    if (translatorContext.getCurrentSection() != TemplateSection.NAVIGATION) {
      return super.renderLineBreak(translatorContext);
    }
    return Markup.empty();
  }

  @Override
  public Markup composeFragmentDefinition(String name, String number, Set<Conditional> conditionals,
      Markup content, Markup children, Set<Parameter> parameters, TranslatorContext translatorContext) {

    if (translatorContext.getCurrentSection() != TemplateSection.NAVIGATION) {
      return super.composeFragmentDefinition(name, number, conditionals, content, children, parameters, translatorContext);
    }

    logger.trace("Composing fragment definition with: name={}, number={}, content={}", name, number, content);

    return generateMarkup(
        FreemarkerTemplate.FRAGMENT_DEFINITION_JSON,
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
  public Markup renderFragmentInvocation(String name, Set<Argument> arguments, TranslatorContext translatorContext) {

    if (translatorContext.getCurrentSection() != TemplateSection.NAVIGATION) {
      return super.renderFragmentInvocation(name, arguments, translatorContext);
    }
    
    logger.trace("Rendering fragment invocation with: name={}", name);

    return generateMarkup(
        FreemarkerTemplate.FRAGMENT_INVOCATION_JSON,
        Pair.of("name", name),
        Pair.of("parameters", arguments));
  }
}