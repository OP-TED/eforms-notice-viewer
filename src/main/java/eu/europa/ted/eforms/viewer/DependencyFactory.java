package eu.europa.ted.eforms.viewer;

import org.antlr.v4.runtime.BaseErrorListener;
import eu.europa.ted.efx.XPathSyntaxMap;
import eu.europa.ted.efx.exceptions.ThrowingErrorListener;
import eu.europa.ted.efx.interfaces.Renderer;
import eu.europa.ted.efx.interfaces.SymbolMap;
import eu.europa.ted.efx.interfaces.SyntaxMap;
import eu.europa.ted.efx.interfaces.TranslatorDependencyFactory;

public class DependencyFactory implements TranslatorDependencyFactory {

  @Override
  public SymbolMap createSymbolMap(String sdkVersion) {
    return SdkSymbolMap.getInstance(sdkVersion);
  }

  @Override
  public SyntaxMap createSyntaxMap() {
    return new XPathSyntaxMap();
  }

  @Override
  public Renderer createRenderer() {
    return new XsltRenderer();
  }

  @Override
  public BaseErrorListener createErrorListener() {
    return new ThrowingErrorListener();
  }
}
