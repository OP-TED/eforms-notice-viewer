package eu.europa.ted.eforms.viewer;

import java.nio.file.Path;
import eu.europa.ted.eforms.sdk.SdkConstants;

/**
 * Constant variables for the Notice Viewer application.
 *
 */
public class NoticeViewerConstants {
  private NoticeViewerConstants() {}

  public static final Path DEFAULT_SDK_ROOT_DIR =
      Path.of(System.getProperty("user.home"), SdkConstants.DEFAULT_SDK_ROOT.toString());

  public static final Path DEFAULT_TEMPLATES_ROOT_DIR = Path.of("templates");

  public static final String TEMPLATES_ROOT_DIR_PROPERTY = "templates.root.dir";
  public static final String NV_CACHE_REGION = "nvCache";

  public static final Path OUTPUT_FOLDER_HTML = Path.of("target", "output-html");
  public static final Path OUTPUT_FOLDER_XSL = Path.of("target", "output-xsl");
}
