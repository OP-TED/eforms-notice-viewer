package eu.europa.ted.eforms.viewer.generator.sdk2;

import java.nio.file.Path;

import eu.europa.ted.eforms.sdk.SdkSymbolResolver;
import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;

@SdkComponent(versions = { "2" }, componentType = SdkComponentType.SYMBOL_RESOLVER, qualifier = "json")
public class SdkSymbolResolverForJson extends SdkSymbolResolver {

  public SdkSymbolResolverForJson(final String sdkVersion, final Path sdkRootPath)
      throws InstantiationException {
    super(sdkVersion, sdkRootPath);
  }
}