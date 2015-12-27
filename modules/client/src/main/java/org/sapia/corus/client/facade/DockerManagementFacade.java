package org.sapia.corus.client.facade;

import java.io.InputStream;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.docker.DockerClientException;
import org.sapia.corus.client.services.docker.DockerContainer;
import org.sapia.corus.client.services.docker.DockerImage;
import org.sapia.corus.client.services.docker.DockerManager;

/**
 * Abstracts access to the {@link DockerManager}, cluster-wide.
 * 
 * @author yduchesne
 */
public interface DockerManagementFacade {
  
   /**
   * @param tagMatcher
   *          the {@link ArgMatcher} to use for matching the tags of the Docker images that should be returned.
   * @param cluster
   *          a {@link ClusterInfo} indicating if this operation should be
   *          clustered.
   * @return the {@link DockerImage} instances corresponding to the Docker images whose
   *         name matched the criteria.
   */
  public Results<List<DockerImage>> getImages(ArgMatcher tagMatcher, ClusterInfo cluster) throws DockerClientException;

  
  /**
   * @param nameMatcher 
   *          the {@link ArgMatcher} to use for matching against Docker container names/image names.
   * @param cluster
   *          a {@link ClusterInfo} indicating if this operation should be
   * @return the {@link List} of {@link DockerContainer}s matching the given criterion.
   * @return the {@link DockerContainer} instances corresponding to the Docker images whose
   *         name matched the criteria.
   */
  public Results<List<DockerContainer>> getContainers(ArgMatcher nameMatcher, ClusterInfo cluster) throws DockerClientException;
  
  /**
   * @param tagMatcher
   *          the {@link ArgMatcher} to use for matching the tags of the Docker images that should be removed.
   * @param cluster
   *          a {@link ClusterInfo} indicating if this operation should be
   *          clustered.
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue removeImages(ArgMatcher tagMatcher, ClusterInfo cluster);
  
  /**
   * @param imageName
   *          the name of the image to pull.
   * @param cluster
   *          a {@link ClusterInfo} indicating if this operation should be
   *          clustered.
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue pullImage(String imageName, ClusterInfo cluster);
 
  /**
   * @param imageName the name of the Docker image to fetch.
   * @return a new {@link DockerImageCallback}, corresponding to the Docker image being fetched.
   * @throws DockerClientException if an error occurs while getting the Docker image.
   */
  public InputStream getImagePayload(String imageName) throws DockerClientException;
  
}
