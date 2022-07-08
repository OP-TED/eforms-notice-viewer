package eu.europa.ted.eforms.viewer.map;

import java.io.Serializable;
import java.nio.file.Path;

public interface SdkMap<T> extends Serializable {
  T get(String resourceId);

  T getOrDefault(String resourceId, T defaultValue);

  SdkMap<T> setResourceFilepath(Path resourcePath);
}
