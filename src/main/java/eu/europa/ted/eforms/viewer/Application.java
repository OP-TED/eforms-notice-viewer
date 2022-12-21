package eu.europa.ted.eforms.viewer;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.viewer.helpers.LoggingHelper;
import picocli.CommandLine;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.CommandLine.ParseResult;

/**
 * Entry point.
 */
public class Application {
  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(final String... args) {
    LoggingHelper.installJulToSlf4jBridge();

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
}
