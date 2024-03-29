package eu.europa.ted.eforms.viewer.generator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.function.Supplier;
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

  private static final String MSG_UNDEFINED_EFX_TEMPLATE = "Undefined EFX template";
  private static final String MSG_UNDEFINED_SDK_VERSION = "Undefined SDK version";
  private static final String MSG_UNDEFINED_VIEW_ID = "Undefined view ID";

  private final TranslatorDependencyFactory dependencyFactory;

  /**
   * @param dependencyFactory The dependency factory to provide to
   *        {@link EfxTranslator#translateTemplate} method
   */
  public XslGenerator(final TranslatorDependencyFactory dependencyFactory) {
    Validate.notNull(dependencyFactory, "Undefined dependency factory");

    this.dependencyFactory = dependencyFactory;
  }

  private XslGenerator(final Builder builder) {
    this(builder.dependencyFactory);
  }

  /**
   * Loads the EFX view template from a file and outputs the XSL to a file.
   *
   * @param sdkVersion The target SDK version.
   * @param efxTemplate Path of the EFX template file
   * @param translatorOptions A {@link TranslatorOptions} instance with configuration for the
   *        translation
   * @param forceBuild Forces the re-creation of XSL files
   * @return Path to the generated file
   * @throws IOException If an error occurred while writing the file
   */
  public Path generateFile(final String sdkVersion, final Path efxTemplate,
      final TranslatorOptions translatorOptions, final boolean forceBuild) throws IOException {
    Validate.notNull(efxTemplate, MSG_UNDEFINED_EFX_TEMPLATE);

    final String viewId = efxTemplate.getFileName().toString().replace(".efx", "");

    return doGenerateFile(sdkVersion, viewId, efxTemplate, translatorOptions, forceBuild);
  }

  /**
   * Loads the EFX view template from a file and outputs the XSL to a file.
   *
   * @param sdkVersion The target SDK version.
   * @param viewId Something like "1" or "X02". It will be used to name the generated XSL file.
   * @param efxTemplate Contents of the EFX template as a string
   * @param translatorOptions A {@link TranslatorOptions} instance with configuration for the
   *        translation
   * @param forceBuild Forces the re-creation of XSL files
   * @return Path to the generated file
   * @throws IOException If an error occurred while writing the file
   */
  public Path generateFile(final String sdkVersion, final String viewId, final String efxTemplate,
      final TranslatorOptions translatorOptions, final boolean forceBuild) throws IOException {
    return doGenerateFile(sdkVersion, viewId, efxTemplate, translatorOptions, forceBuild);
  }

  /**
   * Loads the EFX view template from a file and outputs the XSL to a file.
   *
   * @param sdkVersion The target SDK version.
   * @param viewId Something like "1" or "X02". It will be used to name the generated XSL file.
   * @param efxTemplate Path of the EFX template file to be used.
   * @param translatorOptions A {@link TranslatorOptions} instance with configuration for the
   *        translation
   * @param forceBuild Forces the re-creation of XSL files
   * @return Path to the generated file
   * @throws IOException If an error occurred while writing the file
   */
  private Path doGenerateFile(final String sdkVersion, final String viewId,
      final Object efxTemplate, final TranslatorOptions translatorOptions, final boolean forceBuild)
      throws IOException {
    Validate.notNull(efxTemplate, MSG_UNDEFINED_EFX_TEMPLATE);
    Validate.notBlank(viewId, MSG_UNDEFINED_VIEW_ID);
    Validate.notBlank(sdkVersion, MSG_UNDEFINED_SDK_VERSION);

    logger.debug("Writing XSL for SDK version [{}] to file", sdkVersion);

    final Path xslFile =
        Path.of(NoticeViewerConstants.OUTPUT_FOLDER_XSL.toString(), sdkVersion, viewId + ".xsl");

    if (Files.isRegularFile(xslFile) && !forceBuild) {
      logger.warn("XSL file [{}] already exists and will not be re-created", xslFile);
    } else {
      final String translation =
          doGenerateString(sdkVersion, efxTemplate, translatorOptions, forceBuild);

      Files.createDirectories(xslFile.getParent());
      try (BufferedWriter writer =
          new BufferedWriter(
              new FileWriter(xslFile.toFile(), NoticeViewerConstants.DEFAULT_CHARSET))) {
        writer.write(translation);
      }

      logger.debug("Wrote generated XSL for version [{}] to file [{}]", sdkVersion, xslFile);
    }

    return xslFile;
  }

  /**
   * Loads the EFX view template contents from a file and outputs the XSL as a string.
   *
   * @param sdkVersion The target SDK version.
   * @param efxTemplate Path of the EFX template file
   * @param translatorOptions A {@link TranslatorOptions} instance with configuration for the
   *        translation
   * @param forceBuild If true, it forces the re-creation of XSL (re-creates cache entries)
   * @return The generated XSL as a string
   */
  public String generateString(final String sdkVersion, final Path efxTemplate,
      final TranslatorOptions translatorOptions, final boolean forceBuild) {
    return doGenerateString(sdkVersion, efxTemplate, translatorOptions, forceBuild);
  }

  /**
   * Loads the EFX view template contents from a file and outputs the XSL as a string.
   *
   * @param sdkVersion The target SDK version.
   * @param efxTemplate Contents of the EFX template as a string
   * @param translatorOptions A {@link TranslatorOptions} instance with configuration for the
   *        translation
   * @param forceBuild If true, it forces the re-creation of XSL (re-creates cache entries)
   * @return The generated XSL as a string
   */
  public String generateString(final String sdkVersion, final String efxTemplate,
      final TranslatorOptions translatorOptions, final boolean forceBuild) {
    return doGenerateString(sdkVersion, efxTemplate, translatorOptions, forceBuild);
  }

  /**
   * Loads the EFX view template contents from a file and outputs the XSL as a string.
   *
   * @param sdkVersion The target SDK version.
   * @param efxTemplate Path of the EFX template file
   * @param translatorOptions A {@link TranslatorOptions} instance with configuration for the
   *        translation
   * @param forceBuild If true, it forces the re-creation of XSL (re-creates cache entries)
   * @return The generated XSL as a string
   */
  private String doGenerateString(final String sdkVersion, final Object efxTemplate,
      final TranslatorOptions translatorOptions, final boolean forceBuild) {
    Validate.notBlank(sdkVersion, MSG_UNDEFINED_SDK_VERSION);
    Validate.notNull(efxTemplate, MSG_UNDEFINED_EFX_TEMPLATE);

    logger.debug("Generating XSL for SDK version [{}]", sdkVersion);

    Supplier<String> translator = getTemplateTranslator(sdkVersion, efxTemplate, translatorOptions);

    if (forceBuild) {
      CacheHelper.put(NoticeViewerConstants.NV_CACHE_REGION, translator.get(),
          new String[] {sdkVersion, efxTemplate.toString()});
    }

    final String translation = CacheHelper.get(translator, NoticeViewerConstants.NV_CACHE_REGION,
        new String[] {sdkVersion, efxTemplate.toString()});
    logger.debug("Successfully created XSL for SDK version [{}].", sdkVersion);

    return translation;
  }

  /**
   * A callback method for performing the translation.
   *
   * @param sdkVersion The target SDK version
   * @param efxTemplate The EFX template file (path to the file or its contents as a string)
   * @param translatorOptions A {@link TranslatorOptions} instance with configuration for the
   *        translation
   * @return A callback method as a {@link Supplier}
   */
  private Supplier<String> getTemplateTranslator(final String sdkVersion, final Object efxTemplate,
      final TranslatorOptions translatorOptions) {
    Validate.notNull(efxTemplate, MSG_UNDEFINED_EFX_TEMPLATE);

    return () -> {
      try {
        if (efxTemplate instanceof String) {
          return EfxTranslator.translateTemplate(dependencyFactory, sdkVersion,
              (String) efxTemplate, translatorOptions);
        } else if (efxTemplate instanceof Path) {
          return EfxTranslator.translateTemplate(dependencyFactory, sdkVersion, (Path) efxTemplate,
              translatorOptions);
        } else {
          throw new IllegalArgumentException(MessageFormat.format(
              "Unsupported EFX template input type [{0}", efxTemplate.getClass().getName()));
        }
      } catch (InstantiationException | IOException e) {
        throw new RuntimeException(
            MessageFormat.format("Failed to build XSL for SDK version[{1}]", sdkVersion), e);
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
     * @return A configured {@link XslGenerator} instance
     */
    public XslGenerator build() {
      return new XslGenerator(this);
    }
  }
}
