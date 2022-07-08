package eu.europa.ted.eforms.viewer.map.common;

import java.io.IOException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.eforms.sdk.annotation.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentTypeEnum;
import eu.europa.ted.eforms.viewer.helpers.SdkConstants;
import eu.europa.ted.eforms.viewer.map.MapFromJson;
import eu.europa.ted.efx.model.SdkNode;

@SdkComponent(componentType = SdkComponentTypeEnum.NODE_MAP, resourceType = SdkNode.class)
public class SdkNodeMap extends MapFromJson<SdkNode> {
  private static final long serialVersionUID = 1L;

  public SdkNodeMap() throws IOException {
    super();
  }

  @Override
  protected void populateMap(final JsonNode json) {
    final ArrayNode nodes = (ArrayNode) json.get(SdkConstants.FIELDS_JSON_XML_STRUCTURE_KEY);
    for (final JsonNode node : nodes) {
      final SdkNode sdkNode = new SdkNode(node);
      _map.put(sdkNode.getId(), sdkNode);
    }
  }
}
