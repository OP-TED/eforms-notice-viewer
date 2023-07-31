package eu.europa.ted.eforms.viewer;

import java.io.IOException;
import java.nio.file.Path;
import org.antlr.v4.runtime.BaseErrorListener;
import eu.europa.ted.eforms.sdk.ComponentFactory;
import eu.europa.ted.eforms.sdk.resource.SdkDownloader;
import eu.europa.ted.efx.exceptions.ThrowingErrorListener;
import eu.europa.ted.efx.interfaces.MarkupGenerator;
import eu.europa.ted.efx.interfaces.ScriptGenerator;
import eu.europa.ted.efx.interfaces.SymbolResolver;
import eu.europa.ted.efx.interfaces.TranslatorDependencyFactory;
import eu.europa.ted.efx.interfaces.TranslatorOptions;

public class DependencyFactory implements TranslatorDependencyFactory {
  private Path sdkRoot;

  @SuppressWarnings("unused")
  private DependencyFactory() {}

  public DependencyFactory(Path sdkRoot) {
    this.sdkRoot = sdkRoot;
  }

  @Override
  public SymbolResolver createSymbolResolver(String sdkVersion) {
    try {
      SdkDownloader.downloadSdk(sdkVersion, sdkRoot);

      return ComponentFactory.getSymbolResolver(sdkVersion, sdkRoot);
    } catch (InstantiationException | IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public ScriptGenerator createScriptGenerator(String sdkVersion, TranslatorOptions options) {
    try {
      return ComponentFactory.getScriptGenerator(sdkVersion, options);
    } catch (InstantiationException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public MarkupGenerator createMarkupGenerator(String sdkVersion, TranslatorOptions options) {
    try {
      return ComponentFactory.getMarkupGenerator(sdkVersion, options);
    } catch (InstantiationException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public BaseErrorListener createErrorListener() {
    return new ThrowingErrorListener();
  }
}
