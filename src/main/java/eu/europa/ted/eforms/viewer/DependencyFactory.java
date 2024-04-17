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
  final private Path sdkRoot;
  final private boolean sdkSnapshotsAllowed;

  /**
   * Public constructor used by the EfxTranslator does not allow the use of
   * SNAPSHOT versions of the SDK.
   * 
   * @param sdkRoot The root directory where the SDK will be downloaded.
   */
  public DependencyFactory(Path sdkRoot) {
    this(sdkRoot, false);
  }

  /**
   * Subclasses can use this constructor to allow the use of SNAPSHOT versions of
   * the SDK.
   * 
   * @param sdkRoot          The root directory where the SDK will be downloaded.
   * @param resolveSnapshots If true, SNAPSHOT versions of the SDK will be
   *                         downloaded if needed.
   */
  protected DependencyFactory(Path sdkRoot, boolean resolveSnapshots) {
    this.sdkRoot = sdkRoot;
    this.sdkSnapshotsAllowed = resolveSnapshots;
  }

  @Override
  public SymbolResolver createSymbolResolver(String sdkVersion, String qualifier) {
    try {
      SdkDownloader.downloadSdk(sdkVersion, sdkRoot, this.sdkSnapshotsAllowed);

      return ComponentFactory.getSymbolResolver(sdkVersion, qualifier, sdkRoot);
    } catch (InstantiationException | IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public ScriptGenerator createScriptGenerator(String sdkVersion, String qualifier, TranslatorOptions options) {
    try {
      return ComponentFactory.getScriptGenerator(sdkVersion, qualifier, options);
    } catch (InstantiationException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public MarkupGenerator createMarkupGenerator(String sdkVersion, String qualifier, TranslatorOptions options) {
    try {
      return ComponentFactory.getMarkupGenerator(sdkVersion, qualifier, options);
    } catch (InstantiationException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public BaseErrorListener createErrorListener() {
    return new ThrowingErrorListener();
  }
}
