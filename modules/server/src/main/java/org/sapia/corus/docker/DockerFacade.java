package org.sapia.corus.docker;


/**
 * Hides Docker integration details.
 *
 * @author yduchesne
 *
 */
public interface DockerFacade {

  /**
   * @return a {@link DockerClientFacade}.
   *
   * @throws IllegalStateException if Docker integration is not enabled.
   */
  public DockerClientFacade getDockerClient() throws IllegalStateException;

  /**
   * @return <code>true</code> if Docker integration is enabled, <code>false</code> otherwise.
   */
  public boolean isEnabled();
  
  /**
   * @return <code>true</code> if synchronization with the Docker registry is enabled, <code>false</code> otherwise.
   */
  public boolean isRegistrySyncEnabled();
  
  /**
   * @return <code>true</code> if Docker images should be automatically removed upon the undeployment of Docker-enabled distributions.
   */
  public boolean isAutoRemoveEnabled();

}
