package eu.europa.ted.eforms.viewer.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public abstract class MapFromJson<T> extends HashMap<String, T> {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(MapFromJson.class);

  protected MapFromJson(final String sdkVersion, final String jsonPathname) throws IOException {
    this.populateMap(sdkVersion, jsonPathname);
  }

  /**
   * @param sdkVersion
   *          Currently ignored. It will be effective in a later implementation
   */
  private final void populateMap(final String sdkVersion, final String jsonPathname) throws IOException {
    logger.info("Populating maps for context, sdkVersion={}, jsonPathname={}", sdkVersion, jsonPathname);
    final ObjectMapper mapper = buildStandardJacksonObjectMapper();
    try (
        InputStream fieldsJsonInputStream = Files.newInputStream(Path.of(jsonPathname))) {
      if (fieldsJsonInputStream == null) {
        throw new RuntimeException(String.format("File not found: %s", jsonPathname));
      }
      if (fieldsJsonInputStream.available() == 0) {
        throw new RuntimeException(String.format("File is empty: %s", jsonPathname));
      }
      final JsonNode json = mapper.readTree(fieldsJsonInputStream);
      this.populateMap(json);
    }
  }

  abstract void populateMap(final JsonNode json);

  /**
   * @return A reusable Jackson object mapper instance.
   */
  private static ObjectMapper buildStandardJacksonObjectMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    // https://fasterxml.github.io/jackson-annotations/javadoc/2.7/com/fasterxml/jackson/annotation/JsonInclude.Include.html
    // Value that indicates that only properties with non-null values are to be
    // included.
    mapper.setSerializationInclusion(Include.NON_NULL);
    // Value that indicates that only properties with null value,
    // or what is considered empty, are not to be included.
    mapper.setSerializationInclusion(Include.NON_EMPTY);
    return mapper;
  }
}
