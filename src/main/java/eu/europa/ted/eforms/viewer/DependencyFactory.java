package eu.europa.ted.eforms.viewer;

import org.antlr.v4.runtime.BaseErrorListener;
import eu.europa.ted.efx.exceptions.ThrowingErrorListener;
import eu.europa.ted.efx.interfaces.Renderer;
import eu.europa.ted.efx.interfaces.SymbolMap;
import eu.europa.ted.efx.interfaces.TranslatorDependencyFactory;

public class DependencyFactory implements TranslatorDependencyFactory {

  @Override
  public SymbolMap getSymbolMap(String sdkVersion) {
    return SdkSymbolMap.getInstance(sdkVersion);
  }

  @Override
  public Renderer Renderer() {
    return new XsltRenderer();
  }

  @Override
  public BaseErrorListener getErrorListener() {
    return new ThrowingErrorListener();
  }
}
