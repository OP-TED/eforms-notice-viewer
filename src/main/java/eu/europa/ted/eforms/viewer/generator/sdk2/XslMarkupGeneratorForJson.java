package eu.europa.ted.eforms.viewer.generator.sdk2;


import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.eforms.viewer.enums.FreemarkerTemplate;
import eu.europa.ted.eforms.viewer.util.xml.XmlHelper;
import eu.europa.ted.efx.interfaces.TranslatorOptions;
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
}
