package eu.europa.ted.eforms.viewer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.efx.EfxTranslatorOptions;
import eu.europa.ted.efx.interfaces.TranslatorOptions;

/**
 * Constant variables for the Notice Viewer application.
 *
 */
public class NoticeViewerConstants {
  private NoticeViewerConstants() {}

  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  public static final TranslatorOptions DEFAULT_TRANSLATOR_OPTIONS = EfxTranslatorOptions.DEFAULT;

  public static final Path DEFAULT_SDK_ROOT_DIR =
      Path.of(System.getProperty("user.home"), SdkConstants.DEFAULT_SDK_ROOT.toString());

  public static final Path DEFAULT_TEMPLATES_ROOT_DIR = Path.of("templates");
  public static final String TEMPLATES_ROOT_DIR_PROPERTY = "templates.root.dir";
  public static final String NV_CACHE_REGION = "nvCache";

  public static final Path OUTPUT_FOLDER_BUILD = Path.of("build");
  public static final Path OUTPUT_FOLDER_HTML =
      Path.of(OUTPUT_FOLDER_BUILD.toString(), "output-html");
  public static final Path OUTPUT_FOLDER_XSL =
      Path.of(OUTPUT_FOLDER_BUILD.toString(), "output-xsl");
  public static final Path OUTPUT_FOLDER_PROFILER =
      Path.of(OUTPUT_FOLDER_BUILD.toString(), "profiler");
}
