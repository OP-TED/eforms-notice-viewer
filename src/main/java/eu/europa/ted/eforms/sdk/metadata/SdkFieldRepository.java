package eu.europa.ted.eforms.sdk.metadata;

import java.nio.file.Path;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.helpers.MapFromJson;
import eu.europa.ted.efx.model.EfxEntityFactory;
import eu.europa.ted.efx.model.SdkField;

public class SdkFieldRepository extends MapFromJson<SdkField> {
  private static final long serialVersionUID = 1L;

  public SdkFieldRepository(String sdkVersion, Path jsonPath) throws InstantiationException {
    super(sdkVersion, jsonPath);
  }

  @Override
  protected void populateMap(final JsonNode json) throws InstantiationException {
    final ArrayNode fields = (ArrayNode) json.get(SdkConstants.FIELDS_JSON_FIELDS_KEY);
    for (final JsonNode field : fields) {
      final SdkField sdkField = EfxEntityFactory.getSdkField(sdkVersion, field);
      put(sdkField.getId(), sdkField);
    }
  }
}
