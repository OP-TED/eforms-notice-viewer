package eu.europa.ted.eforms.viewer;

import picocli.CommandLine;

/**
 * Entry point.
 */
public class Application {
  public static void main(String... args) {
    int exitCode = new CommandLine(new CliCommand()).execute(args);
    System.exit(exitCode);
  }
}
