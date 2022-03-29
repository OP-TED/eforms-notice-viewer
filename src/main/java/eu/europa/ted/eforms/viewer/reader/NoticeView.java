package eu.europa.ted.eforms.viewer.reader;

import java.util.Objects;

public class NoticeView {

  // TODO add SDK version stuff the way we do for fields.json
  // "sdkVersion" : "eforms-sdk-0.5.0",
  // "metadataDatabase" : {
  // "version" : "0.3.0",
  // "createdOn" : "2022-01-20T16:03:52"
  // },

  private final String viewId;
  private final String name;
  private final String description;
  private final NoticeViewTemplate viewTemplateRoot;

  public NoticeView(final String viewId, final String name, final String description,
      final NoticeViewTemplate viewTemplateRoot) {
    assert viewId != null;
    assert name != null;
    assert viewTemplateRoot != null;
    this.viewId = viewId;
    this.name = name;
    this.description = description;
    this.viewTemplateRoot = viewTemplateRoot;
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
    NoticeView other = (NoticeView) obj;
    return Objects.equals(viewId, other.viewId);
  }

  public String getDescription() {
    return description;
  }

  public String getName() {
    return name;
  }

  public String getViewId() {
    return viewId;
  }

  public NoticeViewTemplate getViewTemplateRoot() {
    return viewTemplateRoot;
  }

  @Override
  public int hashCode() {
    return Objects.hash(viewId);
  }

  /**
   * Use this for debugging or logging, but avoid using it for anything important.
   */
  @Override
  public String toString() {
    return "ViewTemplate [viewId=" + viewId + ", name=" + name + ", root=" + getViewTemplateRoot()
        + "]";
  }
}
