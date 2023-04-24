package eu.europa.ted.eforms.viewer.util;

import java.util.logging.Level;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class LoggingHelper {
  private LoggingHelper() {}

  public static void installJulToSlf4jBridge() {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    java.util.logging.Logger.getLogger("").setLevel(Level.FINEST); // Root logger, for example.
  }
}

