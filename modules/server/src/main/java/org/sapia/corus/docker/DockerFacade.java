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



}
