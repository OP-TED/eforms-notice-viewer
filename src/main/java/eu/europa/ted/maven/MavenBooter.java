package eu.europa.ted.maven;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.util.repository.DefaultProxySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenBooter {
  private static final Logger logger = LoggerFactory.getLogger(MavenBooter.class);

  private static final String SETTINGS_FILE_NAME = "settings.xml";
  private static final Path DEFAULT_LOCAL_REPOSITORY_PATH = Path.of("local-maven-repo");

  private static final MavenBooter INSTANCE = new MavenBooter();

  private final RepositorySystem repositorySystem;
  private final RepositorySystemSession repositorySession;
  private final Settings settings;
  private final List<RemoteRepository> repositories;

  private MavenBooter() {
    repositorySystem = ManualRepositorySystemFactory.newRepositorySystem();

    try {
      settings = getSettings();
      repositorySession = newRepositorySystemSession(repositorySystem, settings);
    } catch (Exception e) {
      logger.error("Failed to configure Maven");
      logger.debug("Cause:", e);
      throw new RuntimeException(e);
    }
    repositories = newRepositories(repositorySession, settings);
  }

  public static VersionRangeResult resolveVersionRange(Artifact artifact) throws VersionRangeResolutionException {
    VersionRangeRequest rangeRequest = new VersionRangeRequest().setArtifact(artifact)
        .setRepositories(MavenBooter.INSTANCE.repositories);

    return MavenBooter.INSTANCE.repositorySystem.resolveVersionRange(MavenBooter.INSTANCE.repositorySession,
        rangeRequest);
  }

  public static File resolveArtifact(Artifact artifact) throws ArtifactResolutionException {
    ArtifactRequest artifactRequest = new ArtifactRequest().setArtifact(artifact)
        .setRepositories(MavenBooter.INSTANCE.repositories);

    return MavenBooter.INSTANCE.repositorySystem
        .resolveArtifact(MavenBooter.INSTANCE.repositorySession, artifactRequest).getArtifact().getFile();

  }

  private static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system, Settings settings) {
    DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession()
        .setProxySelector(getProxySelector(settings));

    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, getLocalRepository(settings)));
    session.setTransferListener(new ConsoleTransferListener());
    session.setRepositoryListener(new ConsoleRepositoryListener());

    return session;
  }

  private static LocalRepository getLocalRepository(Settings settings) {
    Path localRepositoryPath = Path.of(Optional.ofNullable(settings.getLocalRepository())
        .orElse(Path.of(System.getProperty("user.home"), ".m2", "repository").toString()));

    LocalRepository localRepository = new LocalRepository(localRepositoryPath.toString());

    logger.debug("Local repository: {}", localRepository.getBasedir());

    return localRepository;
  }

  private static File getSettingsFile() {
    Path settingsPath = null;

    logger.debug("Looking for settings file using environment variable MAVEN_OPTS.");
    String mavenOpts = System.getenv("MAVEN_OPTS");
    if (StringUtils.isNotBlank(mavenOpts)) {
      String mavenOptsUserHome = Arrays.asList(mavenOpts.split("\\s")).stream()
          .filter((String s) -> s.startsWith("-Duser.home="))
          .map((String s) -> s.replaceAll("-Duser.home=", StringUtils.EMPTY)).findFirst().orElse(StringUtils.EMPTY);

      if (StringUtils.isNotBlank(mavenOptsUserHome)) {
        settingsPath = Path.of(mavenOptsUserHome, ".m2", SETTINGS_FILE_NAME);
      }
    }

    if (settingsPath == null || !Files.exists(settingsPath)) {
      logger.debug("Looking for settings file under user's home folder.");
      settingsPath = Path.of(System.getProperty("user.home"), ".m2", SETTINGS_FILE_NAME);
    }

    if (settingsPath == null || !Files.exists(settingsPath)) {
      logger.debug("Looking for settings file under Maven Home, as set by environment variale M2_HOME.");
      settingsPath = Path.of(System.getenv("M2_HOME"), "conf", SETTINGS_FILE_NAME);
    }

    if (settingsPath == null || !Files.exists(settingsPath)) {
      logger.debug("No settings file found");
      return null;
    } else {
      logger.debug("Settings path: {}", settingsPath);
      return settingsPath.toFile();
    }
  }

  private static Settings getSettings() throws SettingsBuildingException {
    File settingsFile = getSettingsFile();

    if (settingsFile == null) {
      return new Settings();
    }

    DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest().setGlobalSettingsFile(settingsFile);

    return new DefaultSettingsBuilderFactory().newInstance().build(request).getEffectiveSettings();
  }

  private static List<RemoteRepository> newRepositories(RepositorySystemSession session, Settings settings) {
    List<RemoteRepository> remoteRepositories = new ArrayList<>();

    if (settings.getProfiles() != null) {
      List<String> activeProfiles = settings.getActiveProfiles();

      settings.getProfiles().stream().forEach((Profile profile) -> {
        if (StringUtils.isNotBlank(profile.getId()) && activeProfiles.contains(profile.getId())) {
          Optional.ofNullable(profile.getRepositories()).orElse(Collections.emptyList()).stream()
              .forEach((Repository repository) -> remoteRepositories.add(toRemoteRepository(repository, session)));
        }
      });

      if (remoteRepositories.isEmpty()) {
        remoteRepositories.addAll(getDefaultRepositories(session));
      }
    }

    return remoteRepositories;
  }

  private static RemoteRepository toRemoteRepository(Repository repository, RepositorySystemSession session) {
    // need a temp repo to lookup proxy
    RemoteRepository tempRemoteRepository = toRemoteRepositoryBuilder(repository).build();
    org.eclipse.aether.repository.Proxy proxy = session.getProxySelector().getProxy(tempRemoteRepository);

    // now build the actual repo and attach proxy
    return toRemoteRepositoryBuilder(repository).setProxy(proxy).build();
  }

  private static RemoteRepository.Builder toRemoteRepositoryBuilder(Repository repository) {
    return new RemoteRepository.Builder(repository.getId(), "default", repository.getUrl());
  }

  private static List<RemoteRepository> getDefaultRepositories(RepositorySystemSession session) {
    RemoteRepository.Builder builder = new RemoteRepository.Builder("central", "default",
        "https://repo.maven.apache.org/maven2/");

    // need a temp repo to lookup proxy
    RemoteRepository tempDefaultRepository = builder.build();
    org.eclipse.aether.repository.Proxy proxy = session.getProxySelector().getProxy(tempDefaultRepository);

    // now build the actual repo and attach proxy
    RemoteRepository defaultRepository = builder.setProxy(proxy).build();

    return Arrays.asList(defaultRepository);
  }

  private static ProxySelector getProxySelector(Settings settings) {
    DefaultProxySelector selector = new DefaultProxySelector();

    Optional.ofNullable(settings.getProxies()).orElse(Collections.emptyList())
        .forEach((Proxy proxy) -> selector.add(convertProxy(proxy), proxy.getNonProxyHosts()));

    return selector;
  }

  private static org.eclipse.aether.repository.Proxy convertProxy(Proxy settingsProxy) {
    AuthenticationBuilder auth = new AuthenticationBuilder().addUsername(settingsProxy.getUsername())
        .addPassword(settingsProxy.getPassword());

    return new org.eclipse.aether.repository.Proxy(settingsProxy.getProtocol(), settingsProxy.getHost(),
        settingsProxy.getPort(), auth.build());
  }
}
