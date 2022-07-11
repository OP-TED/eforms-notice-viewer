package eu.europa.ted.eforms.viewer.helpers;

import java.nio.file.Path;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.europa.ted.efx.interfaces.SdkNode;
import eu.europa.ted.efx.model.EfxEntityFactory;

public class SdkNodeMap extends MapFromJson<SdkNode> {
  private static final long serialVersionUID = 1L;

  public SdkNodeMap(String sdkVersion, Path jsonPath) throws InstantiationException {
    super(sdkVersion, jsonPath);
  }

  @Override
  protected void populateMap(final JsonNode json) throws InstantiationException {
    final ArrayNode nodes = (ArrayNode) json.get(SdkConstants.FIELDS_JSON_XML_STRUCTURE_KEY);
    for (final JsonNode node : nodes) {
      final SdkNode sdkNode = EfxEntityFactory.getSdkNode(sdkVersion, node);
      put(sdkNode.getId(), sdkNode);
    }
  }
}
