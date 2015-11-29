package org.sapia.corus.docker;

import org.sapia.corus.client.common.log.LogCallback;

import com.spotify.docker.client.DockerClient;

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
