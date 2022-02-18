package eu.europa.ted;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ted.reader.NoticeView;
import eu.europa.ted.reader.NoticeViewReader;
import eu.europa.ted.writer.NoticeViewWriter;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public class EntryPoint {

  /**
   * @param args TODO take XML to view, and view to use
   */
  public static void main(final String[] args) {
    System.out.println("eForms Notice Viewer");
    System.out.println(String.format("args=%s", Arrays.toString(args)));

    // https://github.com/OP-TED/eForms-SDK/tree/main/examples/notices
    // Read json.
    // 1. Just output the HTML template (sections ...)
    // 2. Read the json AND corresponding XML.

    try {
      final ObjectMapper mapper = NoticeViewReader.buildStandardJacksonObjectMapper();

      // Builds a custom Java representation from the JSON.
      final NoticeView noticeView =
          NoticeViewReader.readRootNode(Path.of("notice-templates/24.json"), mapper);

      System.out.println(noticeView);

      // Configure and run the freemarker template engine.
      final Path templateFolder = Path.of("/freemarker-templates");
      final String templateFileToUse = "html5-simple.ftl";
      final Locale locale = Locale.US;
      final Charset charset = StandardCharsets.UTF_8;

      final Configuration conf =
          NoticeViewWriter.buildFreeMarkerConfig(templateFolder, locale, charset);
      NoticeViewWriter.generateFileUsingFreeMarkerTemplate(noticeView,
          templateFolder.resolve(templateFileToUse), Path.of("target/output-html5.html"), conf);

      NoticeViewWriter.javaObjectToJson(noticeView, Path.of("target/output-data.json"), mapper);

    } catch (IOException e) {
      e.printStackTrace();

    } catch (TemplateException e) {
      e.printStackTrace();
    }
  }

}
