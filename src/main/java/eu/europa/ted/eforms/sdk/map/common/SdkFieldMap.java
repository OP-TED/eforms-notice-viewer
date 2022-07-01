package eu.europa.ted.eforms.sdk.map.common;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import eu.europa.ted.eforms.sdk.map.MapFromJson;
import eu.europa.ted.eforms.viewer.helpers.SdkConstants;
import eu.europa.ted.efx.model.SdkField;

public class SdkFieldMap extends MapFromJson<SdkField> {
  private static final long serialVersionUID = 1L;

  public SdkFieldMap(final Path jsonPath) throws IOException {
    super(jsonPath);
  }

  @Override
  protected void populateMap(final JsonNode json) {
    final ArrayNode fields = (ArrayNode) json.get(SdkConstants.FIELDS_JSON_FIELDS_KEY);
    for (final JsonNode field : fields) {
      final SdkField sdkField = new SdkField(field);
      put(sdkField.getId(), sdkField);
    }
  }
}
