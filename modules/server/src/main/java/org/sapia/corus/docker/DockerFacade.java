package org.sapia.corus.docker;

import com.spotify.docker.client.DockerClient;

/**
 * Hides Docker integration details.
 * 
 * @author yduchesne
 *
 */
public interface DockerFacade {

  /**
   * @return a {@link DockerClient}.
   * 
   * @throws IllegalStateException if Docker integration is not enabled.
   */
  public DockerClient getDockerClient() throws IllegalStateException;


}
