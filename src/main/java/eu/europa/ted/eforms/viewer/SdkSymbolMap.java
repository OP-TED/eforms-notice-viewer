package eu.europa.ted.eforms.viewer;


import static java.util.Map.entry;
import java.io.IOException;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import eu.europa.ted.eforms.sdk.SdkCodelist;
import eu.europa.ted.eforms.sdk.SdkField;
import eu.europa.ted.eforms.sdk.SdkNode;
import eu.europa.ted.eforms.viewer.helpers.SdkCodelistMap;
import eu.europa.ted.eforms.viewer.helpers.SdkFieldMap;
import eu.europa.ted.eforms.viewer.helpers.SdkNodeMap;
import eu.europa.ted.eforms.xpath.XPathContextualizer;
import eu.europa.ted.efx.interfaces.SymbolMap;

public class SdkSymbolMap implements SymbolMap {

  protected Map<String, SdkField> fieldById;
  protected Map<String, SdkNode> nodeById;
  protected Map<String, SdkCodelist> codelistById;

  /**
   * Maps efx operators to xPath operators.
   */
  static final Map<String, String> operators = Map.ofEntries(entry("+", "+"), entry("-", "-"),
      entry("*", "*"), entry("/", "div"), entry("%", "mod"), entry("and", "and"), entry("or", "or"),
      entry("not", "not"), entry("==", "="), entry("!=", "!="), entry("<", "<"), entry("<=", "<="),
      entry(">", ">"), entry(">=", ">="));

  /**
   * EfxToXpathSymbols is implemented as a "kind-of" singleton. One instance per version of the
   * eForms SDK.
   */
  private static final Map<String, SdkSymbolMap> instances = new HashMap<>();

  /**
   * Gets the single instance containing the sysmbls definned in the given version of the eForms
   * SDK.
   *
   * @param sdkVersion Version of the SDK
   */
  public static SdkSymbolMap getInstance(final String sdkVersion) {
    return instances.computeIfAbsent(sdkVersion, k -> new SdkSymbolMap(sdkVersion));
  }

  /**
   * Builds EFX list from the passed codelist refence. This will lazily compute and cache the result
   * for reuse as the operation can be costly on some large lists.
   *
   * @param codelistId A reference to an SDK codelist.
   * @return The EFX string representation of the list of all the codes of the referenced codelist.
   */
  public final String expandCodelist(final String codelistId) {
    SdkCodelist codelist = codelistById.get(codelistId);
    if (codelist == null) {
      throw new InputMismatchException(String.format("Codelist '%s' not found.", codelistId));
    }
    return codelist.toString(", ", "(", ")", '\'');
  }

  /**
   * Private, use getInstance method instead.
   *
   * @param sdkVersion The version of the SDK.
   */
  protected SdkSymbolMap(final String sdkVersion) {
    this.loadMapData(sdkVersion);
  }

  protected void loadMapData(final String sdkVersion) {
    try {
      this.fieldById = new SdkFieldMap(sdkVersion);
      this.nodeById = new SdkNodeMap(sdkVersion);
      this.codelistById = new SdkCodelistMap(sdkVersion);
    } catch (IOException e) {
      throw new RuntimeException(
          String.format("Unable to load Symbols for eForms-SDK %s", sdkVersion), e);
    }
  }

  /**
   * Gets the id of the parent node of a given field.
   *
   * @param fieldId The id of the field who's parent node we are looking for.
   * @return The id of the parent node of the given field.
   */
  public String parentNodeOfField(final String fieldId) {
    final SdkField sdkField = fieldById.get(fieldId);
    if (sdkField != null) {
      return sdkField.getParentNodeId();
    }
    throw new InputMismatchException(String.format("Unknown field '%s'", fieldId));
  }

  /**
   * @param fieldId The id of a field.
   * @return The xPath of the given field.
   */
  public String absoluteXpathOfField(final String fieldId) {
    final SdkField sdkField = fieldById.get(fieldId);
    if (sdkField == null) {
      throw new InputMismatchException(String.format("Unknown field identifier '%s'.", fieldId));
    }
    return sdkField.getXpathAbsolute();
  }

  /**
   * @param nodeId The id of a node or a field.
   * @return The xPath of the given node or field.
   */
  public String absoluteXpathOfNode(final String nodeId) {
    final SdkNode sdkNode = nodeById.get(nodeId);
    if (sdkNode == null) {
      throw new InputMismatchException(String.format("Unknown node identifier '%s'.", nodeId));
    }
    return sdkNode.getXpathAbsolute();
  }

  /**
   * Find the context of a rule that applies to a given field. The context of the rule applied to a
   * field, is typically the xPathAbsolute of that field's parent node.
   *
   * @param fieldId The id of the field of which we want to find the context.
   * @return The absolute xPath of the parent node of the passed field
   */
  public String contextPathOfField(String fieldId) {
    return absoluteXpathOfNode(parentNodeOfField(fieldId));
  }

  /**
   * Find the context for nested predicate that applies to a given field taking into account the
   * pre-existing context.
   *
   * @param fieldId
   * @param broaderContextPath
   * @return
   */
  public String contextPathOfField(String fieldId, String broaderContextPath) {
    return relativeXpathOfNode(parentNodeOfField(fieldId), broaderContextPath);
  }

  /**
   * Gets the xPath of the given field relative to the given context.
   *
   * @param fieldId The id of the field for which we want to find the relative xPath.
   * @param contextPath xPath indicating the context.
   * @return The xPath of the given field relative to the given context.
   */
  public String relativeXpathOfField(String fieldId, String contextPath) {
    final String xpath = absoluteXpathOfField(fieldId);
    return XPathContextualizer.contextualize(contextPath, xpath);
  }

  /**
   * Gets the xPath of the given node relative to the given context.
   *
   * @param nodeId The id of the node for which we want to find the relative xPath.
   * @param contextPath XPath indicating the context.
   * @return The XPath of the given node relative to the given context.
   */
  public String relativeXpathOfNode(String nodeId, String contextPath) {
    final String xpath = absoluteXpathOfNode(nodeId);
    return XPathContextualizer.contextualize(contextPath, xpath);
  }

  public String mapOperator(String operator) {
    return SdkSymbolMap.operators.get(operator);
  }

  public String typeOfField(String fieldId) {
    final SdkField sdkField = fieldById.get(fieldId);
    if (sdkField == null) {
      throw new InputMismatchException(String.format("Unknown field '%s'.", fieldId));
    }
    return sdkField.getType();
  }

  public String rootCodelistOfField(String fieldId) {
    final SdkField sdkField = fieldById.get(fieldId);
    if (sdkField == null) {
      throw new InputMismatchException(String.format("Unknown field '%s'.", fieldId));
    }
    return sdkField.getRootCodelistId();
  }
}
