package eu.europa.ted.eforms.viewer;

import java.util.Arrays;

public class Application {

  /**
   * @param args TODO take XML to view, and view to use
   */
  public static void main(final String[] args) {
    System.out.println("eForms Notice Viewer");
    System.out.println(String.format("args=%s", Arrays.toString(args)));

    // https://github.com/OP-TED/eForms-SDK/tree/main/examples/notices
    // Read json.

    // NoticeViewWriter.javaObjectToJson(noticeView, Path.of("target/output-data.json"), mapper);
  }

}
