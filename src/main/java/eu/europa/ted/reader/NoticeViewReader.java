package eu.europa.ted.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class NoticeViewReader {

  private NoticeViewReader() {
    // Utility class.
  }

  /**
   * @return A reusable Jackson object mapper instance.
   */
  public static ObjectMapper buildStandardJacksonObjectMapper() {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    // https://fasterxml.github.io/jackson-annotations/javadoc/2.7/com/fasterxml/jackson/annotation/JsonInclude.Include.html

    // Value that indicates that only properties with non-null values are to be included.
    objectMapper.setSerializationInclusion(Include.NON_NULL);

    // Value that indicates that only properties with null value, or what is considered empty, are
    // not to be included.
    objectMapper.setSerializationInclusion(Include.NON_EMPTY);

    return objectMapper;
  }

  public static NoticeView readRootNode(final Path pathToJsonFile, final ObjectMapper objectMapper)
      throws IOException {
    final File file = pathToJsonFile.toFile();
    final JsonNode tree = objectMapper.readTree(file);

    final String viewId = getTextMaybeNull(tree, "id");
    final String name = getTextMaybeNull(tree, "name");
    final String description = getTextMaybeNull(tree, "description");

    final JsonNode viewTemplateRootJson = tree.get("viewTemplateRoot");
    if (viewTemplateRootJson == null) {
      throw new RuntimeException(String.format("Found no view templates for viewId=%s", viewId));
    }

    // Build a custom Java representation of the tree.
    final NoticeViewTemplate nvt = readChildNodeRecursive(viewTemplateRootJson, 1); // Level one.

    // TODO pass SDK version and more.
    return new NoticeView(viewId, name, description, nvt);
  }

  public static String processViewTemplate(final String viewTemplate) {
    return viewTemplate; // TODO call EFX.
  }

  private static NoticeViewTemplate readChildNodeRecursive(final JsonNode node, final int depth) {
    final String id = getTextMaybeNull(node, "id"); // Could be used as an ID in the HTML.
    final String parentId = getTextMaybeNoKey(node, "parentId");
    final String template = getTextMaybeNull(node, "template");

    final String childrenKey = "children";
    final JsonNode childNodes = node.get(childrenKey);

    final String efxTemplateOutput = processViewTemplate(template);

    // Build a custom Java representation.
    final NoticeViewTemplate nvt;
    if (childNodes == null) {
      nvt = new NoticeViewTemplate(id, parentId, depth, efxTemplateOutput); // Tree leaf.
    } else {
      if (!childNodes.isArray()) {
        throw new RuntimeException(
            String.format("Expecting %s to be a JSON array for id=%s", childrenKey, id));
      }
      final int size = childNodes.size();
      final List<NoticeViewTemplate> nvts = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        nvts.add(readChildNodeRecursive(childNodes.get(i), depth + 1));
      }
      nvt = new NoticeViewTemplate(id, parentId, template, depth, nvts);
    }
    return nvt;
  }

  private static String getTextMaybeNoKey(final JsonNode node, String fieldName) {
    final JsonNode textNode = node.get(fieldName);
    if (textNode == null) {
      return null; // The entire key / value pair is not present.
    }
    return getTextMaybeNull(node, fieldName);
  }

  private static String getTextMaybeNull(final JsonNode node, String fieldName) {
    return node.get(fieldName).asText(null);
  }

}
