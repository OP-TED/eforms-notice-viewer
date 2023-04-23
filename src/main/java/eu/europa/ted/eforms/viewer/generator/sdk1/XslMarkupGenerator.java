package eu.europa.ted.eforms.viewer.generator.sdk1;

import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.efx.interfaces.TranslatorOptions;

@SdkComponent(versions = {"1", "2"}, componentType = SdkComponentType.MARKUP_GENERATOR)
public class XslMarkupGenerator extends eu.europa.ted.eforms.viewer.generator.XslMarkupGenerator {


  public XslMarkupGenerator(TranslatorOptions options) {
    super(options);
  }

  @Override
  protected String[] getAssetTypes() {
    return new String[]  {"business-term", "field", "code", "auxiliary"};
  }
}
