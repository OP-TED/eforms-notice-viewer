package eu.europa.ted.eforms.sdk.map.common;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import eu.europa.ted.eforms.viewer.helpers.MapFromJson;
import eu.europa.ted.eforms.viewer.helpers.SdkConstants;
import eu.europa.ted.eforms.viewer.helpers.SdkResourcesLoader;
import eu.europa.ted.efx.model.SdkNode;

public class SdkNodeMap extends MapFromJson<SdkNode> {
  private static final long serialVersionUID = 1L;

  public SdkNodeMap(final String sdkVersion) throws IOException {
    super(sdkVersion, SdkResourcesLoader.getInstance().getResourceAsPath(SdkConstants.ResourceType.SDK_FIELDS_FIELDS_JSON, sdkVersion).toString());
  }

  @Override
  protected void populateMap(final JsonNode json) {
    final ArrayNode nodes = (ArrayNode) json.get(SdkConstants.FIELDS_JSON_XML_STRUCTURE_KEY);
    for (final JsonNode node : nodes) {
      final SdkNode sdkNode = new SdkNode(node);
      this.put(sdkNode.getId(), sdkNode);
    }
  }
}
