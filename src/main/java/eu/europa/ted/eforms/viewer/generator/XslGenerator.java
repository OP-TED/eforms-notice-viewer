package eu.europa.ted.eforms.viewer.generator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.function.Supplier;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.viewer.DependencyFactory;
import eu.europa.ted.eforms.viewer.NoticeViewerConstants;
import eu.europa.ted.eforms.viewer.util.CacheHelper;
import eu.europa.ted.efx.EfxTranslator;
import eu.europa.ted.efx.interfaces.TranslatorOptions;

public class XslGenerator {
  private static final Logger logger = LoggerFactory.getLogger(XslGenerator.class);

  private static final String MSG_UNDEFINED_EFX_TEMPLATE_INPUT =
      "Undefined input stream for EFX template";
  private static final String MSG_UNDEFINED_SDK_VERSION = "Undefined SDK version";
  private static final String MSG_UNDEFINED_VIEW_ID = "Undefined view ID";

  private final DependencyFactory dependencyFactory;
  private final TranslatorOptions translatorOptions;

  public XslGenerator(final DependencyFactory dependencyFactory,
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
   * Takes the EFX view template as a viewId string and outputs the XSL to a file.
   *
   * @param viewId Something like "1" or "X02", it will try to get the corresponding view template
   *        from SDK by using naming conventions
   * @param forceBuild Forces the re-creation of XSL files
   * @return Path to the generated file
   * @throws FileNotFoundException
   * @throws IOException If an error occurred while writing the file
   */
  public Path generate(final String sdkVersion, final String viewId, final Path efxTemplate,
      final boolean forceBuild) throws IOException {
    Validate.notNull(efxTemplate, "Undefined path to EFX template");

    if (!Files.isRegularFile(efxTemplate)) {
      throw new FileNotFoundException(efxTemplate.toString());
    }

    try (final InputStream efxTemplateInputStream = Files.newInputStream(efxTemplate)) {
      return generate(sdkVersion, viewId, efxTemplateInputStream, forceBuild);
    }
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
  public Path generate(final String sdkVersion, final String viewId,
      final InputStream efxTemplateInputStream, final boolean forceBuild) throws IOException {
    Validate.notBlank(sdkVersion, MSG_UNDEFINED_SDK_VERSION);
    Validate.notNull(viewId, MSG_UNDEFINED_VIEW_ID);

    logger.debug("Generating XSL for view ID [{}] and SDK version [{}]", viewId, sdkVersion);

    final Path xslFile =
        Path.of(NoticeViewerConstants.OUTPUT_FOLDER_XSL.toString(), sdkVersion, viewId + ".xsl");

    if (!Files.isRegularFile(xslFile) || forceBuild) {
      Supplier<String> translator =
          getTemplateTranslator(sdkVersion, efxTemplateInputStream, viewId);

      if (forceBuild) {
        CacheHelper.put(NoticeViewerConstants.NV_CACHE_REGION, translator.get(),
            new String[] {sdkVersion, viewId});
      }

      final String translation =
          CacheHelper.get(translator, NoticeViewerConstants.NV_CACHE_REGION,
              new String[] {sdkVersion, viewId});

      Files.createDirectories(xslFile.getParent());
      try (BufferedWriter writer =
          new BufferedWriter(
              new FileWriter(xslFile.toFile(), NoticeViewerConstants.DEFAULT_CHARSET))) {
        writer.write(translation);
      }

      logger.debug("Successfully created XSL for view ID [{}] and SDK version [{}]: {}", viewId,
          sdkVersion, xslFile);
    }

    return xslFile;
  }

  private Supplier<String> getTemplateTranslator(final String sdkVersion,
      final InputStream efxTemplateInputStream, final String viewId) {
    Validate.notNull(efxTemplateInputStream, MSG_UNDEFINED_EFX_TEMPLATE_INPUT);

    return () -> {
      try {
        return EfxTranslator.translateTemplate(dependencyFactory, sdkVersion,
            efxTemplateInputStream,
            translatorOptions);
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
    private final DependencyFactory dependencyFactory;

    // optional parameters
    private TranslatorOptions translatorOptions;

    public Builder(final DependencyFactory dependencyFactory) {
      this.dependencyFactory = dependencyFactory;
    }

    public static Builder create(DependencyFactory dependencyFactory) {
      return new Builder(dependencyFactory);
    }

    public XslGenerator build() {
      return new XslGenerator(this);
    }

    public Builder withTranslatorOptions(TranslatorOptions translatorOptions) {
      this.translatorOptions = translatorOptions;
      return this;
    }
  }
}
