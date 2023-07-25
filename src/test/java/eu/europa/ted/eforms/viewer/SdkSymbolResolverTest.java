package eu.europa.ted.eforms.viewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.sdk.SdkSymbolResolver;
import eu.europa.ted.eforms.sdk.SdkVersion;

class SdkSymbolResolverTest {
  private static final Logger logger = LoggerFactory.getLogger(SdkSymbolResolverTest.class);

  private static final Path SDK_RESOURCES_ROOT =
      Path.of("src", "test", "resources", "symbol-resolver-test");

  @Test
  void testSymbolResolution() throws InstantiationException {
    final SdkVersion sdkVersion = new SdkVersion("1.0");

    final SdkSymbolResolver resolver =
        new SdkSymbolResolver(sdkVersion.toStringWithoutPatch(), SDK_RESOURCES_ROOT);

    final String fieldId = "BT-10-Procedure-Buyer";
    final String rootCodelist = resolver.getRootCodelistOfField(fieldId);

    logger.info("Found rootCodelist={} for field={}", rootCodelist, fieldId);

    assertEquals("main-activity", rootCodelist);
  }
}
