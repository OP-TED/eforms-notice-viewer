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
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.resource.SdkResourceLoader;
import eu.europa.ted.eforms.viewer.DependencyFactory;
import eu.europa.ted.eforms.viewer.NoticeViewerConstants;
import eu.europa.ted.eforms.viewer.util.CacheHelper;
import eu.europa.ted.efx.EfxTranslator;
import eu.europa.ted.efx.interfaces.TranslatorOptions;

public class XslGenerator {
  private static final Logger logger = LoggerFactory.getLogger(XslGenerator.class);

  private final Path sdkRoot;
  private final TranslatorOptions translatorOptions;

  public XslGenerator(final Path sdkRoot, final TranslatorOptions translatorOptions) {
    this.sdkRoot = sdkRoot;
    this.translatorOptions = ObjectUtils.defaultIfNull(translatorOptions,
        NoticeViewerConstants.DEFAULT_TRANSLATION_OPTIONS);
  }

  private XslGenerator(final Builder builder) {
    this(builder.sdkRoot, builder.translatorOptions);
  }

  /**
   * Takes the EFX view template as a viewId string and outputs the XSL to a file.
   *
   * @param viewId Something like "1" or "X02", it will try to get the corresponding view template
   *        from SDK by using naming conventions
   * @param forceBuild Forces the re-creation of XSL files
   * @return Path to the generated file
   * @throws FileNotFoundException
   * @throws IOException If an error occurred while writing the file
   */
  public Path generate(final String sdkVersion, final String viewId, final boolean forceBuild)
      throws IOException {
    logger.debug("Generating XSL for view ID [{}] and SDK version [{}]", viewId, sdkVersion);

    final Path viewPath = getEfxPath(sdkVersion, viewId);

    logger.debug("View path: {}", viewPath);

    if (!Files.isRegularFile(viewPath)) {
      throw new FileNotFoundException(viewPath.toString());
    }

    final Path xslFile =
        Path.of(NoticeViewerConstants.OUTPUT_FOLDER_XSL.toString(), sdkVersion, viewId + ".xsl");

    if (!Files.isRegularFile(xslFile) || forceBuild) {
      try (InputStream viewInputStream = Files.newInputStream(viewPath)) {
        Supplier<String> translator = getTemplateTranslator(sdkVersion, viewInputStream, viewId);

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
   */
  private Path getEfxPath(final String sdkVersion, final String viewId) {
    return SdkResourceLoader.getResourceAsPath(sdkVersion, SdkConstants.SdkResource.VIEW_TEMPLATES,
        viewId + ".efx", sdkRoot);
  }

  private Supplier<String> getTemplateTranslator(final String sdkVersion,
      final InputStream viewInputStream, final String viewId) {
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

  public static final class Builder {
    // required parameters

    // optional parameters
    private Path sdkRoot;
    private TranslatorOptions translatorOptions;

    public Builder() {}

    public static Builder create() {
      return new Builder();
    }

    public XslGenerator build() {
      return new XslGenerator(this);
    }

    public Builder withSdkRoot(final Path sdkRoot) {
      this.sdkRoot = sdkRoot;
      return this;
    }

    public Builder withTranslatorOptions(TranslatorOptions translatorOptions) {
      this.translatorOptions = translatorOptions;
      return this;
    }
  }
}
