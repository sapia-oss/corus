package org.sapia.corus.docker;

import java.nio.file.Paths;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.LogCallback;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.util.DynamicProperty;
import org.sapia.corus.util.DynamicProperty.DynamicPropertyListener;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.AuthConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.ProgressMessage;
import com.spotify.docker.client.messages.RemovedImage;
import com.spotify.docker.client.messages.Version;

/**
 * Implementation of the {@link DockerFacade} interface with the spotify docker client.
 *
 * @author yduchesne
 */
public class SpotifyDockerClientFacade implements DockerFacade {

  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

  private final InternalConfigurator configurator;

  private DynamicProperty<Boolean> enabled          = new DynamicProperty<Boolean>();
  private DynamicProperty<String>  email            = new DynamicProperty<String>();
  private DynamicProperty<String>  username         = new DynamicProperty<String>();
  private DynamicProperty<String>  password         = new DynamicProperty<String>();
  private DynamicProperty<String>  serverAddress    = new DynamicProperty<String>();
  private DynamicProperty<String>  daemonUri        = new DynamicProperty<String>();
  private DynamicProperty<String>  certificatesPath = new DynamicProperty<String>();

  private final Object lock = new Object();
  private volatile DockerClient dockerClient;

  /**
   * Creates a new {@link SpotifyDockerClientFacade} instance.
   *
   * @param configurator The corus configurator.
   */
  public SpotifyDockerClientFacade(InternalConfigurator configurator) {
    this.configurator = configurator;
  }

  // --------------------------------------------------------------------------
  // Config setters

  public void setEnabled(boolean enabled) {
    this.enabled.setValue(enabled);
  }

  public void setEmail(String email) {
    this.email.setValue(email);
  }

  public void setPassword(String password) {
    this.password.setValue(password);
  }

  public void setServerAddress(String serverAddress) {
    this.serverAddress.setValue(serverAddress);
  }

  public void setUsername(String username) {
    this.username.setValue(username);
  }

  public void setDaemonUri(String uri) {
    this.daemonUri.setValue(uri);
  }

  public void setCertificatesPath(String path) {
    this.certificatesPath.setValue(path);
  }

  // --------------------------------------------------------------------------
  // Lifecyle

  @PostConstruct
  public void init() {
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DOCKER_ENABLED, enabled);
    enabled.addListener(new DynamicPropertyListener<Boolean>() {
      @Override
      public void onModified(DynamicProperty<Boolean> property) {
        onDockerConfigChange();
      }
    });

    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DOCKER_CLIENT_EMAIL, email);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DOCKER_CLIENT_USERNAME, username);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DOCKER_CLIENT_PASSWORD, password);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DOCKER_REGISTRY_ADDRESS, serverAddress);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DOCKER_DAEMON_URI, daemonUri);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DOCKER_CERTIFICATES_PATH, certificatesPath);
    DynamicPropertyListener<String> propertyChangeListener = new DynamicPropertyListener<String>() {
      @Override
      public void onModified(DynamicProperty<String> property) {
        onDockerConfigChange();
      }
    };
    email.addListener(propertyChangeListener);
    username.addListener(propertyChangeListener);
    password.addListener(propertyChangeListener);
    serverAddress.addListener(propertyChangeListener);
    daemonUri.addListener(propertyChangeListener);
    certificatesPath.addListener(propertyChangeListener);

    if (enabled.getValueNotNull()) {
      log.info("Docker integration enabled");
    } else {
      log.info("Docker integration disabled");
    }
  }

  @PreDestroy
  public void shutdown() {
    log.info("Shutting down docker facade...");
    if (dockerClient != null) {
      log.info("Closing current docker client connection & resources...");
      dockerClient.close();
      dockerClient = null;
    }
  }

  protected void onDockerConfigChange() {
    log.info("Detecting docker config change...");
    if (dockerClient != null) {
      synchronized (lock) {
        try {
          log.info("Closing current docker client connection & resources...");
          dockerClient.close();
        } finally {
          dockerClient = null;
        }
      }
    }
  }

  protected DockerClient internalGetDockerClient() throws IllegalStateException, DockerCertificateException {
    synchronized (lock) {
      if (dockerClient == null) {
        log.info("Creating new docker client with configuration:"
            + "\n\temail=" + email.getValue()
            + "\n\tusername=" + username.getValue()
            + "\n\tregistryServer=" + serverAddress.getValue()
            + "\n\tdaemonUri=" + daemonUri.getValue()
            + "\n\tcertificatesPath=" + certificatesPath.getValue());

        AuthConfig auth = AuthConfig.builder()
            .email(email.getValueNotNull())
            .username(username.getValueNotNull())
            .password(password.getValueNotNull())
            .serverAddress(serverAddress.getValueNotNull())
            .build();

        DefaultDockerClient.Builder clientBuilder = DefaultDockerClient.builder()
            .authConfig(auth)
            .uri(daemonUri.getValueNotNull());

        if (StringUtils.isNotBlank(certificatesPath.getValue())) {
          clientBuilder.dockerCertificates(new DockerCertificates(Paths.get(certificatesPath.getValueNotNull())));
        }

        dockerClient = clientBuilder.build();
      }
    }

    return dockerClient;
  }

  // --------------------------------------------------------------------------
  // DockerFacade interface

  @Override
  public DockerClient getDockerClient() throws IllegalStateException {
    if (!enabled.getValueNotNull()) {
      throw new IllegalStateException("Docker integration disabled: cannot create Docker client");
    }
    AuthConfig auth = AuthConfig.builder()
        .email(email.getValueNotNull())
        .username(username.getValueNotNull())
        .password(password.getValueNotNull())
        .serverAddress(serverAddress.getValueNotNull())
        .build();

    return DefaultDockerClient.builder().authConfig(auth).build();
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.docker.DockerFacade#pullImage(java.lang.String, org.sapia.corus.client.common.LogCallback)
   */
  @Override
  public void pullImage(String imageName, final LogCallback logCallback) {
    // Validation
    if (StringUtils.isBlank(imageName)) {
      throw new IllegalArgumentException("Docker image name passed in cannot be null or blank");
    }
    if (!enabled.getValueNotNull()) {
      throw new IllegalStateException("Docker integration disabled: cannot pull docker image");
    }

    try {
      log.info("Pulling docker image '" + imageName + "' from remote registry...");

      DockerClient client = internalGetDockerClient();

      client.pull(imageName, new ProgressHandler() {
        @Override
        public void progress(ProgressMessage msg) throws DockerException {
          log.info("DOCKER >> " + msg.toString());
          if (logCallback != null) {
            if (msg.error() == null) {
              logCallback.error("DOCKER >> " + msg.error());
            } else {
              StringBuilder builder = new StringBuilder()
                  .append("DOCKER >> ")
                  .append(msg.status());
              if (msg.progress() != null) {
                builder.append(" : ").append(msg.status());
              }
              logCallback.debug(builder.toString());
            }
          }
        }
      });

    } catch (Exception e) {
      throw new DockerFacadeException("System error pulling docker image '" + imageName + "' from remote registry", e);
    }
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.docker.DockerFacade#removeImage(java.lang.String, org.sapia.corus.client.common.LogCallback)
   */
  @Override
  public void removeImage(String imageName, LogCallback callback) {
    // Validation
    if (StringUtils.isBlank(imageName)) {
      throw new IllegalArgumentException("Docker image name passed in cannot be null or blank");
    }
    if (!enabled.getValueNotNull()) {
      throw new IllegalStateException("Docker integration disabled: cannot remove docker image");
    }

    try {
      log.info("Removing docker image '" + imageName + "' from local daemon...");

      DockerClient client = internalGetDockerClient();

      List<RemovedImage> response = client.removeImage(imageName);

      if (response.isEmpty()) {
        log.warn("No docker image removed from local daemon");
        callback.error("No docker image removed from local daemon");
      } else {
        for (RemovedImage ri: response) {
          log.info("Removed docker image " + ri.toString());
          callback.debug("Removed docker image id " + ri.imageId());
        }
      }

    } catch (Exception e) {
      throw new DockerFacadeException("System error removing docker image '" + imageName + "' from local daemon", e);
    }
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.docker.DockerFacade#createContainer()
   */
  @Override
  public String createContainer() {
    return "";
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.docker.DockerFacade#startContainer(java.lang.String, org.sapia.corus.client.common.LogCallback)
   */
  @Override
  public void startContainer(String containerId, LogCallback callback) {
    // Validation
    if (StringUtils.isBlank(containerId)) {
      throw new IllegalArgumentException("Docker container id passed in cannot be null or blank");
    }
    if (!enabled.getValueNotNull()) {
      throw new IllegalStateException("Docker integration disabled: cannot start container");
    }

    try {
      log.info("Starting docker container '" + containerId + "' from local daemon...");

      DockerClient client = internalGetDockerClient();

      client.startContainer(containerId);
      log.info("Docker container " + containerId + " started");
      callback.debug("Docker container " + containerId + " started");

    } catch (Exception e) {
      callback.error("Error starting docker container " + containerId + " ==> " + e.getMessage());
      throw new DockerFacadeException("System error starting docker container '" + containerId + "' from local daemon", e);
    }
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.docker.DockerFacade#stopContainer(java.lang.String, int, org.sapia.corus.client.common.LogCallback)
   */
  @Override
  public void stopContainer(String containerId, int timeoutSeconds, LogCallback callback) {
    // Validation
    if (StringUtils.isBlank(containerId)) {
      throw new IllegalArgumentException("Docker container id passed in cannot be null or blank");
    }
    if (!enabled.getValueNotNull()) {
      throw new IllegalStateException("Docker integration disabled: cannot stop container");
    }

    try {
      log.info("Stopping docker container '" + containerId + "'...");
      callback.debug("Stopping container " + containerId + "...");

      internalGetDockerClient().stopContainer(containerId, timeoutSeconds);

      log.info("Docker container " + containerId + " stopped");
      callback.debug("Docker container " + containerId + " stopped");

    } catch (Exception e) {
      callback.error("Error stopping docker container " + containerId + " ==> " + e.getMessage());
      throw new DockerFacadeException("System error stopping docker container '" + containerId + "' from local daemon", e);
    }
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.docker.DockerFacade#removeContainer(java.lang.String, org.sapia.corus.client.common.LogCallback)
   */
  @Override
  public void removeContainer(String containerId, LogCallback callback) {
    // Validation
    if (StringUtils.isBlank(containerId)) {
      throw new IllegalArgumentException("Docker container id passed in cannot be null or blank");
    }
    if (!enabled.getValueNotNull()) {
      throw new IllegalStateException("Docker integration disabled: cannot remove container");
    }

    try {
      log.info("Removing docker container '" + containerId + "'...");
      callback.debug("Removing container " + containerId + "...");

      internalGetDockerClient().removeContainer(containerId, true);

      log.info("Docker container " + containerId + " removed");
      callback.debug("Docker container " + containerId + " removed");

    } catch (Exception e) {
      callback.error("Error removing docker container " + containerId + " ==> " + e.getMessage());
      throw new DockerFacadeException("System error removing docker container '" + containerId + "' from local daemon", e);
    }
  }


  public String ping() {
    try {
      log.info("Ping docker client...");

      DockerClient client = internalGetDockerClient();
      return client.ping();

    } catch (Exception e) {
      throw new DockerFacadeException("System error accessing docker client", e);
    }
  }

  public void version() {
    try {
      log.info("Version docker client...");

      DockerClient client = internalGetDockerClient();
      Version response = client.version();
      log.info("DOCKER >> " + response.toString());

      Info response2 = client.info();
      log.info("DOCKER >> " + response2.toString());

    } catch (Exception e) {
      throw new DockerFacadeException("System error accessing docker client", e);
    }
  }

  public void getAllImages() {
    try {
      log.info("Getting all local docker images...");

      DockerClient client = internalGetDockerClient();
      List<Image> response = client.listImages(ListImagesParam.allImages(false));
      log.info("DOCKER >> Got " + response.size() + " image(s)");
      for (Image im: response) {
        log.info("DOCKER >> " + im.toString());
      }

    } catch (Exception e) {
      throw new DockerFacadeException("System error accessing docker client", e);
    }

  }
}
