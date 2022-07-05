package eu.europa.ted.eforms.sdk.map;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public abstract class MapFromJson<T> implements SdkMap<T> {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(MapFromJson.class);

  protected transient Map<String, T> _map;

  protected MapFromJson() throws IOException {
    _map = new HashMap<>();
  }

  @Override
  public T get(String resourceId) {
    return _map.get(resourceId);
  }

  @Override
  public T getOrDefault(String resourceId, T defaultValue) {
    return _map.getOrDefault(resourceId, defaultValue);
  }

  @Override
  public SdkMap<T> setResourceFilepath(Path jsonPath) {
    try {
      populateMap(jsonPath);
    } catch (IOException e) {
      throw new RuntimeException(MessageFormat.format("Failed to set resource filepath to [{}]", jsonPath, e));
    }

    return this;
  }

  private final void populateMap(final Path jsonPath) throws IOException {
    logger.info("Populating maps for context, jsonPath={}", jsonPath);
    final ObjectMapper mapper = buildStandardJacksonObjectMapper();
    try (InputStream fieldsJsonInputStream = Files.newInputStream(jsonPath)) {
      if (fieldsJsonInputStream == null) {
        throw new RuntimeException(String.format("File not found: %s", jsonPath));
      }
      if (fieldsJsonInputStream.available() == 0) {
        throw new RuntimeException(String.format("File is empty: %s", jsonPath));
      }
      final JsonNode json = mapper.readTree(fieldsJsonInputStream);
      populateMap(json);
    }
  }

  protected abstract void populateMap(final JsonNode json);

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
