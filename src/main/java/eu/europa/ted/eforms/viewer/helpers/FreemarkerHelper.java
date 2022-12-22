package eu.europa.ted.eforms.viewer.helpers;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.viewer.config.NoticeViewerConfig;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerHelper {
  private static final Logger logger = LoggerFactory.getLogger(FreemarkerHelper.class);

  private FreemarkerHelper() {}

  /**
   * Locates a Freemarker template by its path.
   *
   * @param templatePath The template's path
   * @return A Freemarker template
   * @throws IOException
   */
  public static Template getTemplate(final String templatePath) throws IOException {
    return NoticeViewerConfig.getFreemarkerConfig().getTemplate(templatePath);
  }

  /**
   * Processes a Freemarker template using a specified map of variables (model).
   * 
   * @param templatePath The template's path
   * @param model The map of variables to load
   * @param out A writer for the output
   * @throws TemplateException
   * @throws IOException
   */
  public static void processTemplate(final String templatePath, final Map<String, Object> model,
      final Writer out) throws TemplateException, IOException {
    logger.trace("Processing Freemarker template [{}]", templatePath);

    getTemplate(templatePath).process(model, out);

    logger.trace("Finished processing Freemarker template [{}]", templatePath);
    logger.trace("Generated output:\n{}", out);
  }
}
