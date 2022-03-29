package eu.europa.ted.eforms.viewer.reader;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a view template node, it can have one or more child nodes.
 *
 * @author rouschr
 */
public class NoticeViewTemplate {
  private final String id;
  private final String parentId;
  private final String template;
  private final List<NoticeViewTemplate> childNodes;
  private final int depth;

  /**
   * Leaf constructor.
   *
   * @param depth
   */
  public NoticeViewTemplate(final String id, final String parentId, final int depth,
      final String template) {
    this(id, parentId, template, depth, Collections.emptyList());
  }

  /**
   * Non-leaf constructor.
   */
  public NoticeViewTemplate(final String id, final String parentId, final String template,
      final int depth, final List<NoticeViewTemplate> childNodes) {
    assert id != null;
    assert template != null;
    assert childNodes != null;
    this.id = id;
    this.parentId = parentId;
    this.template = template;
    this.depth = depth;
    this.childNodes = childNodes;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final NoticeViewTemplate other = (NoticeViewTemplate) obj;
    return Objects.equals(id, other.id);
  }

  public List<NoticeViewTemplate> getChildNodes() {
    return childNodes;
  }

  public String getId() {
    return id;
  }

  public int getDepth() {
    return depth;
  }

  public String getParentId() {
    return parentId;
  }

  public String getTemplate() {
    return template;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  /**
   * Use this for debugging or logging, but avoid using it for anything important.
   */
  @Override
  public String toString() {
    return "ViewTemplateNode [id=" + id + ", parentId=" + parentId + ", template=" + template
        + ", childNodesSize=" + childNodes.size() + "]";
  }
}
