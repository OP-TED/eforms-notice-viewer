/*
 * Copyright 2022 European Union
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European
 * Commission – subsequent versions of the EUPL (the "Licence"); You may not use this work except in
 * compliance with the Licence. You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence
 * is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.europa.ted.eforms.viewer.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration options for profiling different components of the notice viewer.
 */
public enum ProfilerConfig {
  XSLT("xslt", "Enable XSLT transformation profiling"),
  EFX("efx", "Enable EFX template processing profiling"),
  ALL("all", "Enable all profiling options");

  private final String value;
  private final String description;

  ProfilerConfig(String value, String description) {
    this.value = value;
    this.description = description;
  }

  public String getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Parse a comma-separated string of profiling options.
   * 
   * @param input Comma-separated string of profiling options (e.g., "xslt,efx" or "all")
   * @return Set of ProfilerConfig options
   * @throws IllegalArgumentException if any option is invalid
   */
  public static Set<ProfilerConfig> parseOptions(String input) {
    if (input == null || input.trim().isEmpty()) {
      return Set.of(ALL);
    }

    String[] options = input.toLowerCase().split(",");
    Set<ProfilerConfig> configs = Arrays.stream(options)
        .map(String::trim)
        .map(ProfilerConfig::fromValue)
        .collect(Collectors.toSet());

    // If ALL is specified, return all options
    if (configs.contains(ALL)) {
      return Set.of(XSLT, EFX);
    }

    return configs;
  }

  /**
   * Get ProfileConfig from string value.
   * 
   * @param value String value to convert
   * @return ProfilerConfig matching the value
   * @throws IllegalArgumentException if value is invalid
   */
  public static ProfilerConfig fromValue(String value) {
    for (ProfilerConfig config : values()) {
      if (config.value.equalsIgnoreCase(value)) {
        return config;
      }
    }
    throw new IllegalArgumentException("Unknown profiling option: " + value + 
        ". Valid options are: " + Arrays.toString(values()));
  }

  /**
   * Check if XSLT profiling is enabled in the given set.
   */
  public static boolean isXsltProfilerEnabled(Set<ProfilerConfig> configs) {
    return configs.contains(XSLT);
  }

  /**
   * Check if EFX profiling is enabled in the given set.
   */
  public static boolean isEfxProfilerEnabled(Set<ProfilerConfig> configs) {
    return configs.contains(EFX);
  }
}