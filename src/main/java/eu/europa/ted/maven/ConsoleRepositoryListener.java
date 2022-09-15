package eu.europa.ted.maven;

import static java.util.Objects.requireNonNull;

import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simplistic repository listener that logs events to the console.
 */
public class ConsoleRepositoryListener extends AbstractRepositoryListener {
  private static final Logger logger = LoggerFactory.getLogger(ConsoleRepositoryListener.class);

  public ConsoleRepositoryListener() {
  }

  @Override
  public void artifactDeployed(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Deployed {} to {}", event.getArtifact(), event.getRepository());
  }

  @Override
  public void artifactDeploying(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Deploying {} to {}", event.getArtifact(), event.getRepository());
  }

  @Override
  public void artifactDescriptorInvalid(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Invalid artifact descriptor for {}: {}", event.getArtifact(), event.getException().getMessage());
  }

  public void artifactDescriptorMissing(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Missing artifact descriptor for {}", event.getArtifact());
  }

  @Override
  public void artifactInstalled(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Installed {} to {}", event.getArtifact(), event.getFile());
  }

  @Override
  public void artifactInstalling(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Installing {} to {}", event.getArtifact(), event.getFile());
  }

  public void artifactResolved(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Resolved artifact {} from {}", event.getArtifact(), event.getRepository());
  }

  @Override
  public void artifactDownloading(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Downloading artifact {} from {}", event.getArtifact(), event.getRepository());
  }

  @Override
  public void artifactDownloaded(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Downloaded artifact {} from {}", event.getArtifact(), event.getRepository());
  }

  @Override
  public void artifactResolving(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Resolving artifact {}", event.getArtifact());
  }

  @Override
  public void metadataDeployed(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Deployed {} to {}", event.getMetadata(), event.getRepository());
  }

  @Override
  public void metadataDeploying(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Deploying {} to {}", event.getMetadata(), event.getRepository());
  }

  @Override
  public void metadataInstalled(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Installed {} to {}", event.getMetadata(), event.getFile());
  }

  @Override
  public void metadataInstalling(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Installing {} to {}", event.getMetadata(), event.getFile());
  }

  @Override
  public void metadataInvalid(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Invalid metadata {}", event.getMetadata());
  }

  @Override
  public void metadataResolved(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Resolved metadata {} from {}", event.getMetadata(), event.getRepository());
  }

  @Override
  public void metadataResolving(RepositoryEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Resolving metadata {} from {}", event.getMetadata(), event.getRepository());
  }

}
