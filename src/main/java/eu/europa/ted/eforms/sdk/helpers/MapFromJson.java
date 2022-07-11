package eu.europa.ted.eforms.sdk.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
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

  protected final String sdkVersion;

  @SuppressWarnings("unused")
  private MapFromJson() {
    throw new UnsupportedOperationException();
  }

  protected MapFromJson(final String sdkVersion, final Path jsonPath)
      throws InstantiationException {
    this.sdkVersion = sdkVersion;

    try {
      populateMap(jsonPath);
    } catch (IOException e) {
      throw new RuntimeException(MessageFormat
          .format("Failed to set resource filepath to [{0}]. Error was: {1}", jsonPath, e));
    }
  }

  private final void populateMap(final Path jsonPath) throws IOException, InstantiationException {
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

  protected abstract void populateMap(final JsonNode json) throws InstantiationException;

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
