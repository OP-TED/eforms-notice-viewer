package eu.europa.ted.eforms.sdk.entity;

import java.nio.file.Path;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.eforms.sdk.SdkConstants;
import eu.europa.ted.eforms.sdk.helpers.MapFromJson;

public class SdkNodeRepository extends MapFromJson<SdkNode> {
  private static final long serialVersionUID = 1L;

  public SdkNodeRepository(String sdkVersion, Path jsonPath) throws InstantiationException {
    super(sdkVersion, jsonPath);
  }

  @Override
  protected void populateMap(final JsonNode json) throws InstantiationException {
    final ArrayNode nodes = (ArrayNode) json.get(SdkConstants.FIELDS_JSON_XML_STRUCTURE_KEY);
    for (final JsonNode node : nodes) {
      final SdkNode sdkNode = SdkEntityFactory.getSdkNode(sdkVersion, node);
      put(sdkNode.getId(), sdkNode);
    }
  }
}
