package eu.europa.ted.eforms.viewer;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.SdkSymbolResolver;
import eu.europa.ted.eforms.sdk.SdkVersion;
import eu.europa.ted.eforms.sdk.resource.SdkDownloader;

@Disabled // NOTE: this downloads an SDK.
public class SdkSymbolResolverTest {

  private static final Logger logger = LoggerFactory.getLogger(SdkSymbolResolverTest.class);

  private static final Path SDK_RESOURCES_ROOT =
      Path.of("target", SdkConstants.DEFAULT_SDK_ROOT.toString());

  @SuppressWarnings("static-method")
  @Test
  public void test() throws InstantiationException, IOException {
    final Path sdkRootPath = SDK_RESOURCES_ROOT;

    final SdkVersion sdkVersion = new SdkVersion("1.0");
    SdkDownloader.downloadSdk(sdkVersion, sdkRootPath);

    final SdkSymbolResolver resolver =
        new SdkSymbolResolver(sdkVersion.toStringWithoutPatch(), sdkRootPath);

    String fieldId = "BT-10-Procedure-Buyer";
    final String rootCodelist = resolver.getRootCodelistOfField(fieldId);
    logger.info("Found rootCodelist={} for field={}", rootCodelist, fieldId);
    // 15:33:09.355 [main] INFO e.e.t.e.viewer.SdkSymbolResolverTest - Found
    // rootCodelist=main-activity for field=BT-10-Procedure-Buyer

    final String expected = "main-activity";

    // The test works but the junit assert itself fails with:
    // java.lang.NoClassDefFoundError: org/opentest4j/AssertionFailedError
    // org.junit.jupiter.api.Assertions.assertEquals(expected, rootCodelist);

    if (!expected.equals(rootCodelist)) {
      throw new AssertionError(
          String.format("Expected '%s' but found '%s'", expected, rootCodelist));
    }
  }
}
