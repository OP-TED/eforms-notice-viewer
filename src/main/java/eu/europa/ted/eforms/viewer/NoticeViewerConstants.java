package eu.europa.ted.eforms.viewer;

import java.nio.file.Path;

/**
 * Constant variables for the Notice Viewer application.
 *
 */
public class NoticeViewerConstants {
  private NoticeViewerConstants() {}

  public static final Path DEFAULT_TEMPLATES_ROOT_DIR = Path.of("templates");
  public static final String TEMPLATES_ROOT_DIR_PROPERTY = "templates.root.dir";
  public static final String NV_CACHE_REGION = "nvCache";
}
