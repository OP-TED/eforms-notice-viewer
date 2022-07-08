package eu.europa.ted.eforms.viewer.map;

import java.nio.file.Path;
import eu.europa.ted.eforms.sdk.component.SdkComponentTypeEnum;
import eu.europa.ted.eforms.sdk.factory.AbstractSdkObjectFactory;
import eu.europa.ted.efx.model.SdkCodelist;
import eu.europa.ted.efx.model.SdkField;
import eu.europa.ted.efx.model.SdkNode;

public class SdkMapFactory extends AbstractSdkObjectFactory {
  public static final SdkMapFactory INSTANCE = new SdkMapFactory();

  private SdkMapFactory() {
    super();
  }

  @SuppressWarnings("unchecked")
  public static SdkMap<SdkCodelist> getCodelistsMap(String sdkVersion, Path codelistsPath)
      throws InstantiationException {
    return SdkMapFactory.INSTANCE
        .getComponentImpl(sdkVersion, SdkComponentTypeEnum.CODELIST_MAP, SdkMap.class)
        .setResourceFilepath(codelistsPath);
  }

  @SuppressWarnings("unchecked")
  public static SdkMap<SdkField> getFieldsMap(String sdkVersion, Path jsonPath)
      throws InstantiationException {
    return SdkMapFactory.INSTANCE
        .getComponentImpl(sdkVersion, SdkComponentTypeEnum.FIELD_MAP, SdkMap.class)
        .setResourceFilepath(jsonPath);
  }

  @SuppressWarnings("unchecked")
  public static SdkMap<SdkNode> getNodesMap(String sdkVersion, Path jsonPath)
      throws InstantiationException {
    return SdkMapFactory.INSTANCE
        .getComponentImpl(sdkVersion, SdkComponentTypeEnum.NODE_MAP, SdkMap.class)
        .setResourceFilepath(jsonPath);
  }
}
