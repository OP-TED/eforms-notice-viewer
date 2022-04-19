package eu.europa.ted.eforms.viewer;

import org.antlr.v4.runtime.BaseErrorListener;
import eu.europa.ted.efx.exceptions.ThrowingErrorListener;
import eu.europa.ted.efx.interfaces.MarkupGenerator;
import eu.europa.ted.efx.interfaces.SymbolResolver;
import eu.europa.ted.efx.interfaces.ScriptGenerator;
import eu.europa.ted.efx.interfaces.TranslatorDependencyFactory;
import eu.europa.ted.efx.xpath.XPathSyntaxMap;

public class DependencyFactory implements TranslatorDependencyFactory {

  @Override
  public SymbolResolver createSymbolResolver(String sdkVersion) {
    return SdkSymbolResolver.getInstance(sdkVersion);
  }

  @Override
  public ScriptGenerator createScriptGenerator() {
    return new XPathSyntaxMap();
  }

  @Override
  public MarkupGenerator createMarkupGenerator() {
    return new XsltRenderer();
  }

  @Override
  public BaseErrorListener createErrorListener() {
    return new ThrowingErrorListener();
  }
}
