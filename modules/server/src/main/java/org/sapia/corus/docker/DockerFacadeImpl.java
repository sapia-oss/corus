package org.sapia.corus.docker;

import javax.annotation.PostConstruct;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.util.DynamicProperty;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.AuthConfig;

/**
 * Implementation of the {@link DockerFacade} interface.
 *
 * @author yduchesne
 *
 */
public class DockerFacadeImpl implements DockerFacade {

  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

  private final InternalConfigurator configurator;

  private DynamicProperty<Boolean> enabled       = new DynamicProperty<Boolean>();
  private DynamicProperty<String>  email         = new DynamicProperty<String>();
  private DynamicProperty<String>  username      = new DynamicProperty<String>();
  private DynamicProperty<String>  password      = new DynamicProperty<String>();
  private DynamicProperty<String>  serverAddress = new DynamicProperty<String>();

  private DynamicProperty<String>  daemonUri = new DynamicProperty<String>();

  /**
   * Creates a new {@link DockerFacadeImpl} instance.
   *
   * @param configurator The corus configurator.
   */
  public DockerFacadeImpl(InternalConfigurator configurator) {
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

  // --------------------------------------------------------------------------
  // Lifecyle

  @PostConstruct
  public void init() {
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DOCKER_ENABLED, enabled);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DOCKER_CLIENT_EMAIL, email);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DOCKER_CLIENT_USERNAME, username);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DOCKER_CLIENT_PASSWORD, password);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DOCKER_REGISTRY_ADDRESS, serverAddress);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_DOCKER_DAEMON_URI, daemonUri);

    if (enabled.getValueNotNull()) {
      log.info("Docker integration enabled");
    } else {
      log.info("Docker integration disabled");
    }

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

    return DefaultDockerClient.builder()
        .authConfig(auth)
        .uri(daemonUri.getValueNotNull())
        .build();
  }

  @Override
  public void pull(String imageName) {
  }

}
