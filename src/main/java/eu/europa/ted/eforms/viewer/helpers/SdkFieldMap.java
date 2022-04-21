package eu.europa.ted.eforms.viewer.helpers;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.efx.model.SdkField;

public class SdkFieldMap extends MapFromJson<SdkField> {

  private static final long serialVersionUID = 1L;

  public SdkFieldMap(final String sdkVersion) throws IOException {
    super(sdkVersion, SdkConstants.EFORMS_SDK_FIELDS_FIELDS_JSON.toString());
  }

  @Override
  protected void populateMap(final JsonNode json) {
    final ArrayNode fields = (ArrayNode) json.get(SdkConstants.FIELDS_JSON_FIELDS_KEY);
    for (final JsonNode field : fields) {
      final SdkField sdkField = new SdkField(field);
      this.put(sdkField.getId(), sdkField);
    }
  }
}
