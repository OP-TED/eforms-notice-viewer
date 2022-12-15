package eu.europa.ted.eforms.viewer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import freemarker.template.Configuration;

public class NoticeViewerConfig {
  private static final Logger logger = LoggerFactory.getLogger(NoticeViewerConfig.class);

  private static Configuration freemarkerConfig;

  private NoticeViewerConfig() {}

  public static Configuration getFreemarkerConfig() {
    if (freemarkerConfig == null) {
      logger.debug("Configuring Freemarker");

      freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
      freemarkerConfig.setClassLoaderForTemplateLoading(ClassLoader.getSystemClassLoader(), "/");

      logger.debug("Freemarker configured successfully.");
    }

    return freemarkerConfig;
  }
}
