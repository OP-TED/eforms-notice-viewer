package eu.europa.ted.eforms.sdk.map.common;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import eu.europa.ted.eforms.sdk.map.MapFromJson;
import eu.europa.ted.eforms.viewer.helpers.SdkConstants;
import eu.europa.ted.efx.model.SdkNode;

public class SdkNodeMap extends MapFromJson<SdkNode> {
  private static final long serialVersionUID = 1L;

  public SdkNodeMap(final Path jsonPath) throws IOException {
    super(jsonPath);
  }

  @Override
  protected void populateMap(final JsonNode json) {
    final ArrayNode nodes = (ArrayNode) json.get(SdkConstants.FIELDS_JSON_XML_STRUCTURE_KEY);
    for (final JsonNode node : nodes) {
      final SdkNode sdkNode = new SdkNode(node);
      put(sdkNode.getId(), sdkNode);
    }
  }
}
