package eu.europa.ted.eforms.viewer.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.function.Supplier;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.viewer.NoticeViewerConstants;
import eu.europa.ted.eforms.viewer.util.CacheHelper;
import eu.europa.ted.efx.EfxTranslator;
import eu.europa.ted.efx.interfaces.TranslatorDependencyFactory;
import eu.europa.ted.efx.interfaces.TranslatorOptions;

/**
 * A XSL generator which uses {@link EfxTranslator} to convert EFX templates to XSL templates.
 */
public class XslGenerator {
  private static final Logger logger = LoggerFactory.getLogger(XslGenerator.class);

  private static final String MSG_UNDEFINED_SDK_VERSION = "Undefined SDK version";
  private static final String MSG_UNDEFINED_VIEW_ID = "Undefined view ID";

  private final TranslatorDependencyFactory dependencyFactory;
  private final TranslatorOptions translatorOptions;

  /**
   * @param dependencyFactory The dependency factory to provide to
   *        {@link EfxTranslator#translateTemplate} method
   * @param translatorOptions A {@link TranslatorOptions} instance with configuration for the
   *        translation
   */
  public XslGenerator(final TranslatorDependencyFactory dependencyFactory,
      final TranslatorOptions translatorOptions) {
    Validate.notNull(dependencyFactory, "Undefined dependency factory");

    this.dependencyFactory = dependencyFactory;
    this.translatorOptions = ObjectUtils.defaultIfNull(translatorOptions,
        NoticeViewerConstants.DEFAULT_TRANSLATOR_OPTIONS);
  }

  private XslGenerator(final Builder builder) {
    this(builder.dependencyFactory, builder.translatorOptions);
  }

  /**
   * Loads the EFX view template contents from an input stream and outputs the XSL to a file.
   *
   * @param sdkVersion The target SDK version.
   * @param viewId Something like "1" or "X02", it will try to get the corresponding view template
   *        from SDK by using naming conventions
   * @param efxTemplate Path of the EFX template file to be used.
   * @param forceBuild Forces the re-creation of XSL files
   * @return Path to the generated file
   * @throws IOException If an error occurred while writing the file
   */
  public Path generateFile(final String sdkVersion, final String viewId,
      final Path efxTemplate, final boolean forceBuild) throws IOException {
    Validate.notBlank(sdkVersion, MSG_UNDEFINED_SDK_VERSION);
    Validate.notNull(viewId, MSG_UNDEFINED_VIEW_ID);

    logger.debug("Writing XSL for view ID [{}] and SDK version [{}] to file", viewId, sdkVersion);

    final Path xslFile =
        Path.of(NoticeViewerConstants.OUTPUT_FOLDER_XSL.toString(), sdkVersion, viewId + ".xsl");

    if (Files.isRegularFile(xslFile) && !forceBuild) {
      logger.warn("XSL file [{}] already exists and will not be re-created", xslFile);
    } else {
      final String translation = generateString(sdkVersion, viewId, efxTemplate, forceBuild);

      Files.createDirectories(xslFile.getParent());
      try (BufferedWriter writer =
          new BufferedWriter(
              new FileWriter(xslFile.toFile(), NoticeViewerConstants.DEFAULT_CHARSET))) {
        writer.write(translation);
      }

      logger.debug("Wrote generated XSL for view ID [{}] and SDK version [{}] to file [{}]", viewId,
          sdkVersion, xslFile);
    }

    return xslFile;
  }

  /**
   * Loads the EFX view template contents from an input stream and outputs the XSL as a string.
   *
   * @param sdkVersion The target SDK version.
   * @param viewId Something like "1" or "X02", it will try to get the corresponding view template
   *        from SDK by using naming conventions
   * @param efxTemplate Path of the EFX template file
   * @param forceBuild If true, it forces the re-creation of XSL (re-creates cache entries)
   * @return The generated XSL as a string
   */
  public String generateString(final String sdkVersion, final String viewId, final Path efxTemplate,
      final boolean forceBuild) {
    Validate.notBlank(sdkVersion, MSG_UNDEFINED_SDK_VERSION);
    Validate.notNull(viewId, MSG_UNDEFINED_VIEW_ID);

    logger.debug("Generating XSL for view ID [{}] and SDK version [{}]", viewId, sdkVersion);

    Supplier<String> translator = getTemplateTranslator(sdkVersion, efxTemplate, viewId);

    if (forceBuild) {
      CacheHelper.put(NoticeViewerConstants.NV_CACHE_REGION, translator.get(),
          new String[] {sdkVersion, viewId});
    }

    final String translation = CacheHelper.get(translator, NoticeViewerConstants.NV_CACHE_REGION,
        new String[] {sdkVersion, viewId});

    logger.debug("Successfully created XSL for view ID [{}] and SDK version [{}].", viewId,
        sdkVersion);

    return translation;
  }

  /**
   * A callback method for performing the translation.
   *
   * @param sdkVersion The target SDK version
   * @param efxTemplate The EFX template file (path to the file or its contents as a string)
   * @param viewId Something like "1" or "X02", it will try to get the corresponding view template
   *        from SDK by using naming conventions
   * @return A callback method as a {@link Supplier}
   */
  private Supplier<String> getTemplateTranslator(final String sdkVersion, final Path efxTemplate,
      final String viewId) {
    return () -> {
      try {
        return EfxTranslator.translateTemplate(dependencyFactory, sdkVersion, efxTemplate,
            translatorOptions);
      } catch (InstantiationException | IOException e) {
        throw new RuntimeException(MessageFormat.format(
            "Failed to build XSL for view ID [{0}] and SDK version[{1}]", viewId, sdkVersion), e);
      }
    };
  }

  /**
   * Builder class for {@link XslGenerator} instances
   */
  public static final class Builder {
    // required parameters
    private final TranslatorDependencyFactory dependencyFactory;

    // optional parameters
    private TranslatorOptions translatorOptions;

    /**
     * @param dependencyFactory The dependency factory to provide to
     *        {@link EfxTranslator#translateTemplate} method
     */
    public Builder(final TranslatorDependencyFactory dependencyFactory) {
      this.dependencyFactory = dependencyFactory;
    }

    /**
     * @param dependencyFactory The dependency factory to provide to
     *        {@link EfxTranslator#translateTemplate} method
     */
    public static Builder create(TranslatorDependencyFactory dependencyFactory) {
      return new Builder(dependencyFactory);
    }

    /**
     * @param translatorOptions A {@link TranslatorOptions} instance with configuration for the
     *        translation
     * @return A {@link Builder} instance
     */
    public Builder withTranslatorOptions(TranslatorOptions translatorOptions) {
      this.translatorOptions = translatorOptions;
      return this;
    }

    /**
     * @return A configured {@link XslGenerator} instance
     */
    public XslGenerator build() {
      return new XslGenerator(this);
    }
  }
}
