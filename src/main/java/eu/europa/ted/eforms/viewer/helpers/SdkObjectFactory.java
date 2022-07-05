package eu.europa.ted.eforms.viewer.helpers;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.atteo.classindex.ClassIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.sdk.annotation.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentDescriptor;
import eu.europa.ted.eforms.sdk.component.SdkComponentTypeEnum;
import eu.europa.ted.eforms.sdk.map.SdkMap;
import eu.europa.ted.efx.model.SdkCodelist;
import eu.europa.ted.efx.model.SdkField;
import eu.europa.ted.efx.model.SdkNode;

public class SdkObjectFactory {
  private static final Logger log = LoggerFactory.getLogger(SdkObjectFactory.class);

  private Map<String, Map<SdkComponentTypeEnum, SdkComponentDescriptor<?>>> componentsMap;

  public static final SdkObjectFactory INSTANCE = new SdkObjectFactory();

  private SdkObjectFactory() {
    populateComponents();
  }

  private void populateComponents() {
    if (componentsMap == null) {
      componentsMap = new HashMap<>();
    }
    ClassIndex.getAnnotated(SdkComponent.class).forEach((Class<?> clazz) -> {
      SdkComponent annotation = clazz.getAnnotation(SdkComponent.class);
      String sdkVersion = annotation.version();
      SdkComponentTypeEnum componentType = annotation.componentType();
      Map<SdkComponentTypeEnum, SdkComponentDescriptor<?>> components =
          componentsMap.get(sdkVersion);
      if (components == null) {
        components = new HashMap<>();
        componentsMap.put(sdkVersion, components);
      }
      SdkComponentDescriptor<?> component =
          new SdkComponentDescriptor<>(sdkVersion, componentType, clazz);
      SdkComponentDescriptor<?> existingComponent = components.get(componentType);
      if (existingComponent != null && !existingComponent.equals(component)) {
        throw new IllegalArgumentException(MessageFormat.format(
            "More than one components of type [{0}] have been found for SDK version [{1}]:\n\t- {2}\n\t- {3}",
            componentType, sdkVersion, existingComponent.getImplType().getName(), clazz.getName()));
      }
      components.put(componentType, component);
    });
  }

  @SuppressWarnings("unchecked")
  public <T> T getComponentImpl(String sdkVersion, SdkComponentTypeEnum componentType,
      Class<T> intf) throws InstantiationException {
    Map<SdkComponentTypeEnum, SdkComponentDescriptor<?>> components = componentsMap.get(sdkVersion);
    if (components == null) {
      log.warn("No SDK components found for SDK [{}]. Trying with fallback SDK ({})", sdkVersion,
          SdkComponent.ANY);
      sdkVersion = SdkComponent.ANY;
      components = componentsMap.get(sdkVersion);
      if (components == null) {
        throw new IllegalArgumentException(MessageFormat
            .format("No SDK component implementations found for SDK version [{0}].", sdkVersion));
      }
    }
    SdkComponentDescriptor<T> descriptor =
        (SdkComponentDescriptor<T>) components.get(componentType);
    if (descriptor == null) {
      throw new IllegalArgumentException(
          MessageFormat.format("No implementation found for component type [{0}] of SDK [{1}]",
              componentType, sdkVersion));
    }
    return descriptor.createInstance();
  }

  @SuppressWarnings("unchecked")
  public static SdkMap<SdkCodelist> getCodelistsMap(String sdkVersion, Path codelistsPath)
      throws InstantiationException {
    return SdkObjectFactory.INSTANCE
        .getComponentImpl(sdkVersion, SdkComponentTypeEnum.CODELIST_MAP, SdkMap.class)
        .setResourceFilepath(codelistsPath);
  }

  @SuppressWarnings("unchecked")
  public static SdkMap<SdkField> getFieldsMap(String sdkVersion, Path jsonPath)
      throws InstantiationException {
    return SdkObjectFactory.INSTANCE
        .getComponentImpl(sdkVersion, SdkComponentTypeEnum.FIELD_MAP, SdkMap.class)
        .setResourceFilepath(jsonPath);
  }

  @SuppressWarnings("unchecked")
  public static SdkMap<SdkNode> getNodesMap(String sdkVersion, Path jsonPath)
      throws InstantiationException {
    return SdkObjectFactory.INSTANCE
        .getComponentImpl(sdkVersion, SdkComponentTypeEnum.NODE_MAP, SdkMap.class)
        .setResourceFilepath(jsonPath);
  }
}
