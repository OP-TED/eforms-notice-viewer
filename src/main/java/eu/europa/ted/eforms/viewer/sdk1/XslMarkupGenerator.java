package eu.europa.ted.eforms.viewer.sdk1;

import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;

@SdkComponent(versions = {"1"}, componentType = SdkComponentType.MARKUP_GENERATOR)
public class XslMarkupGenerator extends eu.europa.ted.eforms.viewer.XslMarkupGenerator {


  public XslMarkupGenerator() {
    super();
  }

  @Override
  protected String[] getAssetTypes() {
    return new String[]  {"business-term", "field", "code", "auxiliary"};
  }
}
