package org.sapia.corus.docker;

import java.nio.file.Paths;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.util.DynamicProperty;
import org.sapia.corus.util.DynamicProperty.DynamicPropertyListener;
import org.springframework.beans.factory.annotation.Autowired;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.messages.AuthConfig;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.Version;

/**
 * Implementation of the {@link DockerFacade} interface with the spotify docker client.
 *
 * @author yduchesne
 */
public class SpotifyDockerFacade implements DockerFacade {
  
  private enum State {
    INIT,
    RUNNING;
  }

  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

  private State state = State.INIT;
  
  @Autowired
  private ServerContext serverContext;
  
  @Autowired
  
  private InternalConfigurator configurator;

  private DynamicProperty<Boolean> enabled          = new DynamicProperty<Boolean>();
  private DynamicProperty<String>  email            = new DynamicProperty<String>();
  private DynamicProperty<String>  username         = new DynamicProperty<String>();
  private DynamicProperty<String>  password         = new DynamicProperty<String>();
  private DynamicProperty<String>  serverAddress    = new DynamicProperty<String>();
  private DynamicProperty<String>  daemonUri        = new DynamicProperty<String>();
  private DynamicProperty<String>  certificatesPath = new DynamicProperty<String>();

  private final Object lock = new Object();
  private volatile DockerClient dockerClient;

  // --------------------------------------------------------------------------
  // Visible for testing
  
  public void setConfigurator(InternalConfigurator configurator) {
    this.configurator = configurator;
  }
  
  public void setServerContext(ServerContext serverContext) {
    this.serverContext = serverContext;
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
    
    state = State.RUNNING;
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

  // --------------------------------------------------------------------------
  // DockerFacade interface
  
  @Override
  public DockerClientFacade getDockerClient() throws IllegalStateException {
    if (!enabled.getValueNotNull()) {
      throw new IllegalStateException("Docker integration disabled: cannot create Docker client");
    }
    try {
      return new SpotifyDockerClientFacade(serverContext, internalGetDockerClient());
    } catch (DockerCertificateException e) {
      throw new IllegalStateException("Error caught pertaining to Docker certificate", e);
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
      throw new DockerFacadeException("System error accessing Docker client", e);
    }
  }

  // --------------------------------------------------------------------------
  // Maintenance-related methods
  
  public List<Container> getContainers(boolean all) {
    try {
      return internalGetDockerClient().listContainers(DockerClient.ListContainersParam.allContainers(true));
    } catch (Exception e) {
      throw new DockerFacadeException("System error accessing Docker client", e);
    }
  }
  
  public List<Image> getAllImages() {
    try {
      return internalGetDockerClient().listImages(ListImagesParam.allImages(false));
    } catch (Exception e) {
      throw new DockerFacadeException("System error accessing Docker client", e);
    }
  }
  
  
  public void clean() {
    try {
      log.info("Stopping all containers and removing all local Docker images...");

      DockerClient client = internalGetDockerClient();

      List<Container> containers = client.listContainers();
      log.info("Got " + containers.size() + " container(s)");

      for (Container c : containers) {
        client.stopContainer(c.id(), 30);
        client.removeContainer(c.id());
      }
      List<Image> response = client.listImages(ListImagesParam.allImages(false));
      log.info("Got " + response.size() + " image(s)");
      for (Image im: response) {
        client.removeImage(im.id(), true, false);
      }
    } catch (Exception e) {
      throw new DockerFacadeException("System error accessing Docker client", e);
    }    
  }
  
  public void stopAllContainers() {
    try {
      DockerClient client = internalGetDockerClient();
  
      List<Container> containers = client.listContainers();
      log.info("Got " + containers.size() + " container(s)");
  
      for (Container c : containers) {
        client.stopContainer(c.id(), 30);
        client.removeContainer(c.id());
      }
    } catch (Exception e) {
      throw new DockerFacadeException("System error accessing Docker client", e);
    }    
  }
  
  // --------------------------------------------------------------------------
  // Restricted methods

  protected void onDockerConfigChange() {
    if (state == State.RUNNING) {
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
}
