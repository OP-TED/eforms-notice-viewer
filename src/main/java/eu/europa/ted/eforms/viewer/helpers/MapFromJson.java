package eu.europa.ted.eforms.viewer.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.europa.ted.eforms.viewer.SdkSymbolMap;

public abstract class MapFromJson<T> extends HashMap<String, T> {

  protected MapFromJson(final String sdkVersion, final String jsonPathname) throws IOException {
    this.populateMap(sdkVersion, jsonPathname);
  }

  /**
   *
   * @param sdkVersion Currently ignored. It will be effective in a later implementation.
   * @throws IOException
   * @throws URISyntaxException
   */
  private final void populateMap(final String sdkVersion, final String jsonPathname)
      throws IOException {
    System.out.println("Populating maps for context, sdkVersion=" + sdkVersion);
    final ObjectMapper objectMapper = buildStandardJacksonObjectMapper();
    final InputStream fieldsJsonInputStream =
        JavaTools.getResourceAsStream(SdkSymbolMap.class.getClassLoader(), jsonPathname);
    if (fieldsJsonInputStream == null) {
      throw new RuntimeException(String.format("File not found: %s", jsonPathname));
    }
    if (fieldsJsonInputStream.available() == 0) {
      throw new RuntimeException(String.format("File is empty: %s", jsonPathname));
    }
    final JsonNode json = objectMapper.readTree(fieldsJsonInputStream);

    this.populateMap(json);
  }

  abstract protected void populateMap(final JsonNode json);

  protected final String getTextNullOtherwise(final JsonNode node, final String key) {
    final JsonNode otherNode = node.get(key);
    if (otherNode == null) {
      return null;
    }
    return otherNode.asText(null);
  }

  /**
   * @return A reusable Jackson object mapper instance.
   */
  private static ObjectMapper buildStandardJacksonObjectMapper() {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    // https://fasterxml.github.io/jackson-annotations/javadoc/2.7/com/fasterxml/jackson/annotation/JsonInclude.Include.html

    // Value that indicates that only properties with non-null values are to be included.
    objectMapper.setSerializationInclusion(Include.NON_NULL);

    // Value that indicates that only properties with null value,
    // or what is considered empty, are not to be included.
    objectMapper.setSerializationInclusion(Include.NON_EMPTY);

    return objectMapper;
  }
}
