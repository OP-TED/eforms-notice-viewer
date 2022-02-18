package eu.europa.ted.writer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ted.reader.NoticeView;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class NoticeViewWriter {

  public static Configuration buildFreeMarkerConfig(final Path templateFolder, final Locale locale,
      final Charset standardCharset) {
    final Configuration cfg = new Configuration(new Version(2, 3, 20));

    // Where do we load the templates from:
    cfg.setClassForTemplateLoading(NoticeViewWriter.class, templateFolder.toString());

    // Some other recommended settings:
    cfg.setDefaultEncoding(standardCharset.toString());
    cfg.setLocale(locale);
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

    return cfg;
  }

  public static String processViewTemplate(final String viewTemplate) {
    return viewTemplate; // TODO
  }

  public static void generateFileUsingFreeMarkerTemplate(final NoticeView noticeView,
      final Path pathToTemplate, final Path outputFilepath, final Configuration freemarkerConfig)
      throws TemplateException, IOException {

    // Template + data-model = output

    // Build model and process template.
    final Map<String, Object> templateModel = new HashMap<>();
    templateModel.put("noticeView", noticeView);

    final Template freemarkerTemplate =
        freemarkerConfig.getTemplate(pathToTemplate.getFileName().toString());

    final Writer consoleWriter = new OutputStreamWriter(System.out);
    freemarkerTemplate.process(templateModel, consoleWriter);

    final Writer fileWriter = new FileWriter(outputFilepath.toFile());
    freemarkerTemplate.process(templateModel, fileWriter);
  }

  public static void javaObjectToJson(final NoticeView noticeView, final Path outputFilepath,
      final ObjectMapper mapper) throws IOException {
    mapper.writeValue(outputFilepath.toFile(), noticeView);
  }
}
