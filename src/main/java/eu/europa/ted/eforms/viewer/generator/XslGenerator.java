package eu.europa.ted.eforms.viewer.generator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.resource.SdkDownloader;
import eu.europa.ted.eforms.sdk.resource.SdkResourceLoader;
import eu.europa.ted.eforms.viewer.DependencyFactory;
import eu.europa.ted.eforms.viewer.NoticeViewerConstants;
import eu.europa.ted.eforms.viewer.util.CacheHelper;
import eu.europa.ted.efx.EfxTranslator;
import eu.europa.ted.efx.interfaces.TranslatorOptions;

public class XslGenerator {
  private static final Logger logger = LoggerFactory.getLogger(XslGenerator.class);

  private final String sdkVersion;
  private final Path sdkRoot;
  private final TranslatorOptions translatorOptions;

  public XslGenerator(final String sdkVersion, final Path sdkRoot,
      final TranslatorOptions translatorOptions) throws IOException {
    SdkDownloader.downloadSdk(sdkVersion, sdkRoot);

    this.sdkVersion = sdkVersion;
    this.sdkRoot = sdkRoot;
    this.translatorOptions = translatorOptions;
  }

  /**
   * Takes the EFX view template as a viewId string and outputs the XSL to a file.
   *
   * @param viewId Something like "1" or "X02", it will try to get the corresponding view template
   *        from SDK by using naming conventions
   * @param forceBuild Forces the re-creation of XSL files
   * @return Path to the generated file
   * @throws IOException If an error occurred while writing the file
   * @throws InstantiationException
   */
  public Path generate(final String viewId, final boolean forceBuild)
      throws IOException, InstantiationException {
    logger.debug("Generating XSL for view ID [{}] and SDK version [{}]", viewId, sdkVersion);

    final Path viewPath = getEfxPath(viewId);

    logger.debug("View path: {}", viewPath);

    if (!Files.isRegularFile(viewPath)) {
      throw new FileNotFoundException(viewPath.toString());
    }

    final Path xslFile =
        Path.of(NoticeViewerConstants.OUTPUT_FOLDER_XSL.toString(), sdkVersion, viewId + ".xsl");

    if (!Files.isRegularFile(xslFile) || forceBuild) {
      try (InputStream viewInputStream = Files.newInputStream(viewPath)) {
        Supplier<String> translator = getTemplateTranslator(viewInputStream, viewId);

        if (forceBuild) {
          CacheHelper.put(NoticeViewerConstants.NV_CACHE_REGION, translator.get(),
              new String[] {sdkRoot.toString(), sdkVersion, viewId});
        }

        final String translation =
            CacheHelper.get(translator, NoticeViewerConstants.NV_CACHE_REGION,
                new String[] {sdkRoot.toString(), sdkVersion, viewId});

        Files.createDirectories(xslFile.getParent());
        try (BufferedWriter writer =
            new BufferedWriter(new FileWriter(xslFile.toFile(), StandardCharsets.UTF_8))) {
          writer.write(translation);
        }

        logger.debug("Successfully created XSL for view ID [{}] and SDK version [{}]: {}", viewId,
            sdkVersion, xslFile);
      }
    }

    return xslFile;
  }

  /**
   * @param viewId It can correspond to a view id, as long as there is one view id per notice id, or
   *        something else for custom views
   * @throws IOException
   */
  private Path getEfxPath(final String viewId) throws IOException {
    return SdkResourceLoader.getResourceAsPath(sdkVersion, SdkConstants.SdkResource.VIEW_TEMPLATES,
        viewId + ".efx", sdkRoot);
  }

  private Supplier<String> getTemplateTranslator(final InputStream viewInputStream,
      final String viewId) {
    return () -> {
      try {
        return EfxTranslator.translateTemplate(new DependencyFactory(sdkRoot), sdkVersion,
            viewInputStream, translatorOptions);
      } catch (InstantiationException | IOException e) {
        throw new RuntimeException(
            MessageFormat.format(
                "Failed to build XSL for view ID [{0}] and SDK version[{1}]",
                viewId, sdkVersion),
            e);
      }
    };
  }
}
