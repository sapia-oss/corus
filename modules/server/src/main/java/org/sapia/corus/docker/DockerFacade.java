package org.sapia.corus.docker;

import org.sapia.corus.client.common.LogCallback;

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

  public void pullImage(String imageName, LogCallback callback);

  public void removeImage(String imageName, LogCallback callback);

  public String createContainer();

  public void startContainer(String containerId, LogCallback callback);

  public void stopContainer(String containerId, int timeoutMillis, LogCallback callback);

  public void removeContainer(String containerId, LogCallback callback);

}
