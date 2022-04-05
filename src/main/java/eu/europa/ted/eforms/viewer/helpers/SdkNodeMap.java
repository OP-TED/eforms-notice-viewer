package eu.europa.ted.eforms.viewer.helpers;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.eforms.sdk.SdkNode;

public class SdkNodeMap extends MapFromJson<SdkNode> {

  public SdkNodeMap(String sdkVersion) throws IOException {
    super(sdkVersion, "eforms-sdk/fields/fields.json");
  }

  @Override
  protected void populateMap(JsonNode json) {
    final ArrayNode nodes = (ArrayNode) json.get("xmlStructure");
    for (final JsonNode node : nodes) {
      SdkNode sdkNode = new SdkNode(node);
      this.put(sdkNode.getId(), sdkNode);
    }
  }
}
