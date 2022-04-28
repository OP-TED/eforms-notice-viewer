package eu.europa.ted.eforms.viewer;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import eu.europa.ted.eforms.viewer.helpers.SdkCodelistMap;
import eu.europa.ted.eforms.viewer.helpers.SdkFieldMap;
import eu.europa.ted.eforms.viewer.helpers.SdkNodeMap;
import eu.europa.ted.efx.interfaces.SymbolResolver;
import eu.europa.ted.efx.model.SdkCodelist;
import eu.europa.ted.efx.model.SdkField;
import eu.europa.ted.efx.model.SdkNode;
import eu.europa.ted.efx.model.Expression.PathExpression;
import eu.europa.ted.efx.xpath.XPathContextualizer;

public class SdkSymbolResolver implements SymbolResolver {

  protected Map<String, SdkField> fieldById;
  protected Map<String, SdkNode> nodeById;
  protected Map<String, SdkCodelist> codelistById;

  /**
   * EfxToXpathSymbols is implemented as a "kind-of" singleton. One instance per version of the
   * eForms SDK.
   */
  private static final Map<String, SdkSymbolResolver> instances = new HashMap<>();

  /**
   * Gets the single instance containing the sysmbls definned in the given version of the eForms
   * SDK.
   *
   * @param sdkVersion Version of the SDK
   */
  public static SdkSymbolResolver getInstance(final String sdkVersion) {
    return instances.computeIfAbsent(sdkVersion, k -> new SdkSymbolResolver(sdkVersion));
  }

  /**
   * Builds EFX list from the passed codelist refence. This will lazily compute and cache the result
   * for reuse as the operation can be costly on some large lists.
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
   */
  protected SdkSymbolResolver(final String sdkVersion) {
    this.loadMapData(sdkVersion);
  }

  protected void loadMapData(final String sdkVersion) {
    try {
      this.fieldById = new SdkFieldMap(sdkVersion);
      this.nodeById = new SdkNodeMap(sdkVersion);
      this.codelistById = new SdkCodelistMap(sdkVersion);
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
  public String parentNodeOfField(final String fieldId) {
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
  public PathExpression absoluteXpathOfField(final String fieldId) {
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
  public PathExpression absoluteXpathOfNode(final String nodeId) {
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
  public PathExpression relativeXpathOfField(String fieldId, PathExpression contextPath) {
    final PathExpression xpath = absoluteXpathOfField(fieldId);
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
  public PathExpression relativeXpathOfNode(String nodeId, PathExpression contextPath) {
    final PathExpression xpath = absoluteXpathOfNode(nodeId);
    return XPathContextualizer.contextualize(contextPath, xpath);
  }

  @Override
  public String typeOfField(String fieldId) {
    final SdkField sdkField = fieldById.get(fieldId);
    if (sdkField == null) {
      throw new ParseCancellationException(String.format("Unknown field '%s'.", fieldId));
    }
    return sdkField.getType();
  }

  @Override
  public String rootCodelistOfField(String fieldId) {
    final SdkField sdkField = fieldById.get(fieldId);
    if (sdkField == null) {
      throw new ParseCancellationException(String.format("Unknown field '%s'.", fieldId));
    }
    return sdkField.getRootCodelistId();
  }
}
