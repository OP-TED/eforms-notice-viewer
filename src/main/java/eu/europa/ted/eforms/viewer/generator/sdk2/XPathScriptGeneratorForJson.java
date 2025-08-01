package eu.europa.ted.eforms.viewer.generator.sdk2;

import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.efx.interfaces.TranslatorOptions;
import eu.europa.ted.efx.xpath.XPathScriptGenerator;

@SdkComponent(versions = {"2"}, componentType = SdkComponentType.SCRIPT_GENERATOR, qualifier = "json")
public class XPathScriptGeneratorForJson extends XPathScriptGenerator {

  public XPathScriptGeneratorForJson(TranslatorOptions translatorOptions) {
    super(translatorOptions);
  }
}