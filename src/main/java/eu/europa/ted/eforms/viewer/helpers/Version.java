package eu.europa.ted.eforms.viewer.helpers;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public class Version implements Comparable<Version> {
  private String major;
  private String minor;
  private String patch;

  public Version() {
    this(null);
  }

  public Version(final String version) {
    Validate.notBlank(version, "Undefined version");
    Validate.matchesPattern(version, "[0-9]+(\\.[0-9]+(-SNAPSHOT)?)*", "Invalid version format");

    String[] versionParts = version.split("\\.");
    this.major = versionParts[0];
    this.minor = versionParts.length > 1 ? versionParts[1] : StringUtils.EMPTY;
    this.patch = versionParts.length > 2 ? versionParts[2] : StringUtils.EMPTY;
  }

  public String getMajor() {
    return major;
  }

  public String getMinor() {
    return minor;
  }

  public String getPatch() {
    return patch;
  }

  public String getNextMajor() {
    return String.valueOf(major + 1);
  }

  public String getNextMinor() {
    return MessageFormat.format("{0}.{1}", major, minor + 1);
  }

  public String toStringWithoutPatch() {
    return StringUtils.join(Arrays.asList(major, minor), ".");
  }

  @Override
  public String toString() {
    return StringUtils.join(Arrays.asList(major, minor, patch), ".");
  }

  @Override
  public int compareTo(Version that) {
    if (that == null) {
      return 1;
    }

    if (this.equals(that)) {
      return 0;
    }

    if (getAsInt(this.getMajor()) == getAsInt(that.getMajor())) {
      if (getAsInt(this.getMinor()) == getAsInt(that.getMinor())) {
        return getAsInt(this.getPatch()) < getAsInt(that.getPatch()) ? -1 : 1;
      } else {
        return getAsInt(this.getMinor()) < getAsInt(that.getMinor()) ? -1 : 1;
      }
    } else {
      return getAsInt(this.getMajor()) < getAsInt(that.getMajor()) ? -1 : 1;
    }
  }

  private int getAsInt(String versionPart) {
    return Integer.parseInt(Optional.ofNullable(versionPart).orElse("0").replace("-SNAPSHOT", ""));
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor, patch);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Version other = (Version) obj;
    return Objects.equals(major, other.major) && Objects.equals(minor, other.minor)
        && Objects.equals(patch, other.patch);
  }
}
