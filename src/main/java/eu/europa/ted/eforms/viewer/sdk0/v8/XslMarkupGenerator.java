package eu.europa.ted.eforms.viewer.sdk0.v8;

import eu.europa.ted.eforms.sdk.selector.component.VersionDependentComponent;
import eu.europa.ted.eforms.sdk.selector.component.VersionDependentComponentType;

@VersionDependentComponent(versions = {"0.8"}, componentType = VersionDependentComponentType.MARKUP_GENERATOR)
public class XslMarkupGenerator extends eu.europa.ted.eforms.viewer.XslMarkupGenerator {


  public XslMarkupGenerator() {
    super();
  }

  @Override
  protected String[] getAssetTypes() {
    return new String[]  {"business-term", "field", "code", "auxiliary"};
  }
}
