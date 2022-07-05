package eu.europa.ted.eforms.viewer;

import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import eu.europa.ted.eforms.sdk.map.SdkMap;
import eu.europa.ted.eforms.viewer.helpers.SdkConstants;
import eu.europa.ted.eforms.viewer.helpers.SdkObjectFactory;
import eu.europa.ted.eforms.viewer.helpers.SdkResourcesLoader;
import eu.europa.ted.efx.interfaces.SymbolResolver;
import eu.europa.ted.efx.model.Expression.PathExpression;
import eu.europa.ted.efx.model.SdkCodelist;
import eu.europa.ted.efx.model.SdkField;
import eu.europa.ted.efx.model.SdkNode;
import eu.europa.ted.efx.xpath.XPathContextualizer;

public class SdkSymbolResolver implements SymbolResolver {
  protected SdkMap<SdkField> fieldById;

  protected SdkMap<SdkNode> nodeById;

  protected SdkMap<SdkCodelist> codelistById;

  /**
   * EfxToXpathSymbols is implemented as a "kind-of" singleton. One instance per version of the
   * eForms SDK.
   */
  private static final Map<String, SdkSymbolResolver> instances = new HashMap<>();

  /**
   * Gets the single instance containing the symbols defined in the given version of the eForms SDK.
   *
   * @param sdkVersion Version of the SDK
   */
  public static SdkSymbolResolver getInstance(final String sdkVersion) {
    return instances.computeIfAbsent(sdkVersion, k -> {
      try {
        return new SdkSymbolResolver(sdkVersion);
      } catch (InstantiationException e) {
        throw new RuntimeException(MessageFormat.format(
            "Failed to instantiate SDK Symbol Resolver for SDK version [{0}]", sdkVersion), e);
      }
    });
  }

  /**
   * Builds EFX list from the passed codelist reference. This will lazily compute and cache the
   * result for reuse as the operation can be costly on some large lists.
   *
   * @param codelistId A reference to an SDK codelist.
   * @return The EFX string representation of the list of all the codes of the referenced codelist.
   */
  @Override
  public final List<String> expandCodelist(final String codelistId) {
    final SdkCodelist codelist = codelistById.get(codelistId);
    if (codelist == null) {
      throw new ParseCancellationException(String.format("Codelist '%s' not found.", codelistId));
    }
    return codelist.getCodes();
  }

  /**
   * Private, use getInstance method instead.
   *
   * @param sdkVersion The version of the SDK.
   * @throws InstantiationException
   */
  protected SdkSymbolResolver(final String sdkVersion) throws InstantiationException {
    this.loadMapData(sdkVersion);
  }

  protected void loadMapData(final String sdkVersion) throws InstantiationException {
    try {
      Path jsonPath = SdkResourcesLoader.INSTANCE
          .getResourceAsPath(SdkConstants.ResourceType.SDK_FIELDS_FIELDS_JSON, sdkVersion);
      Path codelistsPath = SdkResourcesLoader.INSTANCE
          .getResourceAsPath(SdkConstants.ResourceType.CODELISTS, sdkVersion);
      this.fieldById = SdkObjectFactory.getFieldsMap(sdkVersion, jsonPath);
      this.nodeById = SdkObjectFactory.getNodesMap(sdkVersion, jsonPath);
      this.codelistById = SdkObjectFactory.getCodelistsMap(sdkVersion, codelistsPath);
    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Unable to load Symbols for eForms-SDK version=%s", sdkVersion), e);
    }
  }

  /**
   * Gets the id of the parent node of a given field.
   *
   * @param fieldId The id of the field who's parent node we are looking for.
   * @return The id of the parent node of the given field.
   */
  @Override
  public String getParentNodeOfField(final String fieldId) {
    final SdkField sdkField = fieldById.get(fieldId);
    if (sdkField != null) {
      return sdkField.getParentNodeId();
    }
    throw new ParseCancellationException(String.format("Unknown field '%s'", fieldId));
  }

  /**
   * @param fieldId The id of a field.
   * @return The xPath of the given field.
   */
  @Override
  public PathExpression getAbsolutePathOfField(final String fieldId) {
    final SdkField sdkField = fieldById.get(fieldId);
    if (sdkField == null) {
      throw new ParseCancellationException(
          String.format("Unknown field identifier '%s'.", fieldId));
    }
    return new PathExpression(sdkField.getXpathAbsolute());
  }

  /**
   * @param nodeId The id of a node or a field.
   * @return The xPath of the given node or field.
   */
  @Override
  public PathExpression getAbsolutePathOfNode(final String nodeId) {
    final SdkNode sdkNode = nodeById.get(nodeId);
    if (sdkNode == null) {
      throw new ParseCancellationException(String.format("Unknown node identifier '%s'.", nodeId));
    }
    return new PathExpression(sdkNode.getXpathAbsolute());
  }

  /**
   * Gets the xPath of the given field relative to the given context.
   *
   * @param fieldId The id of the field for which we want to find the relative xPath.
   * @param contextPath xPath indicating the context.
   * @return The xPath of the given field relative to the given context.
   */
  @Override
  public PathExpression getRelativePathOfField(String fieldId, PathExpression contextPath) {
    final PathExpression xpath = getAbsolutePathOfField(fieldId);
    return XPathContextualizer.contextualize(contextPath, xpath);
  }

  /**
   * Gets the xPath of the given node relative to the given context.
   *
   * @param nodeId The id of the node for which we want to find the relative xPath.
   * @param contextPath XPath indicating the context.
   * @return The XPath of the given node relative to the given context.
   */
  @Override
  public PathExpression getRelativePathOfNode(String nodeId, PathExpression contextPath) {
    final PathExpression xpath = getAbsolutePathOfNode(nodeId);
    return XPathContextualizer.contextualize(contextPath, xpath);
  }

  @Override
  public String getTypeOfField(String fieldId) {
    final SdkField sdkField = fieldById.get(fieldId);
    if (sdkField == null) {
      throw new ParseCancellationException(String.format("Unknown field '%s'.", fieldId));
    }
    return sdkField.getType();
  }

  @Override
  public String getRootCodelistOfField(String fieldId) {
    final SdkField sdkField = fieldById.get(fieldId);
    if (sdkField == null) {
      throw new ParseCancellationException(String.format("Unknown field '%s'.", fieldId));
    }
    return sdkField.getRootCodelistId();
  }
}
