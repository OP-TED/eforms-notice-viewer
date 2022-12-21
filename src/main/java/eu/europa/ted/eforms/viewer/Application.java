package eu.europa.ted.eforms.viewer;

import java.util.Arrays;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

/**
 * Entry point.
 */
public class Application {
  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(final String... args) {
    installJulToSlf4jBridge();

    CommandLine cli = new CommandLine(new CliCommand());
    cli.setExecutionExceptionHandler(new IExecutionExceptionHandler() {
      @Override
      public int handleExecutionException(Exception ex, CommandLine commandLine,
          ParseResult parseResult)
          throws Exception {
        logger.error("Error executing the application with arguments [{}]. Please see the logs.",
            Arrays.asList(args));
        logger.debug("Exception thrown:", ex);

        return 0;
      }
    });

    int exitCode = cli.execute(args);
    System.exit(exitCode);
  }

  private static void installJulToSlf4jBridge() {
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
    java.util.logging.Logger.getLogger("").setLevel(Level.FINEST); // Root logger, for example.
  }
}
