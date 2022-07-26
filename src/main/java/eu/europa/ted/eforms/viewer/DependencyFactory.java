package eu.europa.ted.eforms.viewer;

import org.antlr.v4.runtime.BaseErrorListener;
import eu.europa.ted.efx.exceptions.ThrowingErrorListener;
import eu.europa.ted.efx.interfaces.MarkupGenerator;
import eu.europa.ted.efx.interfaces.ScriptGenerator;
import eu.europa.ted.efx.interfaces.SymbolResolver;
import eu.europa.ted.efx.interfaces.TranslatorDependencyFactory;

public class DependencyFactory implements TranslatorDependencyFactory {
  @Override
  public SymbolResolver createSymbolResolver(String sdkVersion) {
    try {
      return ComponentFactory.getSymbolResolver(sdkVersion);
    } catch (InstantiationException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public ScriptGenerator createScriptGenerator(String sdkVersion) {
    try {
      return ComponentFactory.getScriptGenerator(sdkVersion);
    } catch (InstantiationException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public MarkupGenerator createMarkupGenerator(String sdkVersion) {
    try {
      return ComponentFactory.getMarkupGenerator(sdkVersion);
    } catch (InstantiationException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public BaseErrorListener createErrorListener() {
    return new ThrowingErrorListener();
  }
}
