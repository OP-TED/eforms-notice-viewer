package eu.europa.ted.eforms.viewer.util;

import java.util.logging.Level;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Utility class with methods for configuring logging.
 */
public class LoggingHelper {
  private LoggingHelper() {}

  /**
   * Installs a bridge between JUL (Java Logging API) and SLF4J.
   * <p>
   * All log messages from libraries (e.g., JCS) which uses JUL will be handled by SLF4J.
   */
  public static void installJulToSlf4jBridge() {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    java.util.logging.Logger.getLogger("").setLevel(Level.FINEST); // Root logger, for example.
  }
}

