package eu.europa.ted.maven;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManualRepositorySystemFactory {
  private static final Logger logger = LoggerFactory.getLogger(ManualRepositorySystemFactory.class);

  private ManualRepositorySystemFactory() {
  }

  public static RepositorySystem newRepositorySystem() {
    /*
     * Aether's components implement org.eclipse.aether.spi.locator.Service to ease
     * manual wiring and using the prepopulated DefaultServiceLocator, we only need
     * to register the repository connector and transporter factories.
     */
    DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator()
        .addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class)
        .addService(TransporterFactory.class, FileTransporterFactory.class)
        .addService(TransporterFactory.class, HttpTransporterFactory.class);

    locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
      @Override
      public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
        logger.debug("Service creation failed for {} with implementation {}", type, impl, exception);
      }
    });

    return locator.getService(RepositorySystem.class);
  }
}
