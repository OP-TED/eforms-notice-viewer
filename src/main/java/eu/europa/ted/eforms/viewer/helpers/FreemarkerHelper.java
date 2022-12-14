package eu.europa.ted.eforms.viewer.helpers;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.viewer.config.NoticeViewerConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerHelper {
  private static final Logger logger = LoggerFactory.getLogger(FreemarkerHelper.class);

  private FreemarkerHelper() {}

  public static void processTemplate(String templatePath, Map<String, Object> model, Writer out)
      throws IOException, TemplateException {
    logger.debug("Processing Freemarker template [{}]", templatePath);

    final Configuration freemarkerConfig = NoticeViewerConfig.getFreemarkerConfig();
    final Template template = freemarkerConfig.getTemplate(templatePath);

    template.process(model, out);

    logger.debug("Finished processing Freemarker template [{}]", templatePath);
    logger.trace("Generated output:\n{}", out);
  }
}
