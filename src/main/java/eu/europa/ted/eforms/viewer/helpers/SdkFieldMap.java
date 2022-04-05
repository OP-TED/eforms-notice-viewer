package eu.europa.ted.eforms.viewer.helpers;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.eforms.sdk.SdkField;

public class SdkFieldMap extends MapFromJson<SdkField> {

  public SdkFieldMap(String sdkVersion) throws IOException {
    super(sdkVersion, "eforms-sdk/fields/fields.json");
  }

  @Override
  protected void populateMap(JsonNode json) {
    final ArrayNode fields = (ArrayNode) json.get("fields");
    for (final JsonNode field : fields) {
      SdkField sdkField = new SdkField(field);
      this.put(sdkField.getId(), sdkField);
    }
  }
}
