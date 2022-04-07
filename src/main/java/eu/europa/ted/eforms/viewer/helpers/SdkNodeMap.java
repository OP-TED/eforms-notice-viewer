package eu.europa.ted.eforms.viewer.helpers;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.eforms.sdk.SdkNode;

public class SdkNodeMap extends MapFromJson<SdkNode> {

  private static final long serialVersionUID = 1L;

  public SdkNodeMap(final String sdkVersion) throws IOException {
    super(sdkVersion, EformsSdkConstants.EFORMS_SDK_FIELDS_FIELDS_JSON.toString());
  }

  @Override
  protected void populateMap(final JsonNode json) {
    final ArrayNode nodes = (ArrayNode) json.get(EformsSdkConstants.FIELDS_JSON_XML_STRUCTURE_KEY);
    for (final JsonNode node : nodes) {
      final SdkNode sdkNode = new SdkNode(node);
      this.put(sdkNode.getId(), sdkNode);
    }
  }
}
