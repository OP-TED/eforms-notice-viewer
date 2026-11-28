package eu.europa.ted.eforms.viewer.generator.sdk2;

import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.efx.interfaces.TranslatorOptions;

/**
 * Generates the XSLT of an EFX-2 view template.
 *
 * <p>The toolkit gives each line its nesting level, its heading level and the body of its
 * navigation id in its own section or callable template, and each call what it puts above the
 * fragment it invokes: a nesting level offset, a heading level offset and a navigation id prefix.
 * The generator passes them to the templates as they are (see the generator of EFX-1, which does
 * the same for EFX-1). A template is displayed wherever it is invoked, so the stylesheet carries
 * them from call to call, and the templates write from them where each line is displayed (see
 * fragment_definition.ftl and fragment_invocation.ftl).
 */
@SdkComponent(versions = {"2"}, componentType = SdkComponentType.MARKUP_GENERATOR)
public class XslMarkupGenerator extends eu.europa.ted.eforms.viewer.generator.sdk1.XslMarkupGenerator {

  public XslMarkupGenerator(TranslatorOptions translatorOptions) {
    super(translatorOptions);
  }
}
