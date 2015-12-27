package org.sapia.corus.client.services.docker;

import java.io.IOException;
import java.rmi.Remote;
import java.util.List;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ProgressQueue;

/**
 * Provides methods for interacting with the Docker daemon.
 * 
 * @author yduchesne
 *
 */
public interface DockerManager extends Remote, Module {
  
  public static final String ROLE = DockerManager.class.getName();
  
  /**
   * @param tagMatcher an {@link ArgMatcher} representing a Docker image tag pattern.
   * @return the {@link List} of {@link DockerImage}s that match the given tag pattern.
   * @throws DockerClientException if a problem occurs while interacting with Docker. 
   */
  public List<DockerImage> getImages(ArgMatcher tagMatcher) throws DockerClientException;
  
  /**
   * @param nameMatcher the {@link ArgMatcher} to use for matching against Docker container names/image names.
   * @return the {@link List} of {@link DockerContainer}s matching the given criterion.
   * @throws IOException if an I/O error occurs while performing this operation.
   * @throws DockerClientException if a problem occurs while interacting with Docker. 
   */
  public List<DockerContainer> getContainers(ArgMatcher nameMatcher) throws DockerClientException;

  /**
   * @param tagMatcher an {@link ArgMatcher} representing a Docker image tag pattern.
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue removeImages(ArgMatcher tagArgMatcher);
  
  /**
   * @param imageName the name of the Docker image to pull.
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue pullImage(String imageName);

}
