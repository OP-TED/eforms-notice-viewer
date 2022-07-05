package eu.europa.ted.eforms.sdk.map.common;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import eu.europa.ted.eforms.sdk.annotation.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentTypeEnum;
import eu.europa.ted.eforms.sdk.map.MapFromJson;
import eu.europa.ted.eforms.viewer.helpers.SdkConstants;
import eu.europa.ted.efx.model.SdkField;

@SdkComponent(componentType = SdkComponentTypeEnum.FIELD_MAP, resourceType = SdkField.class)
public class SdkFieldMap extends MapFromJson<SdkField> {
  private static final long serialVersionUID = 1L;

  public SdkFieldMap() throws IOException {
    super();
  }

  @Override
  protected void populateMap(final JsonNode json) {
    final ArrayNode fields = (ArrayNode) json.get(SdkConstants.FIELDS_JSON_FIELDS_KEY);
    for (final JsonNode field : fields) {
      final SdkField sdkField = new SdkField(field);
      _map.put(sdkField.getId(), sdkField);
    }
  }
}
