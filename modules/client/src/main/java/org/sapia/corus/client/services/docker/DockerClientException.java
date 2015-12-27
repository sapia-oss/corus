package org.sapia.corus.client.services.docker;

/**
 * Throw in the context of interacting with Docker, through the {@link DockerManager}.
 * 
 * @author yduchesne
 *
 */
public class DockerClientException extends Exception {
  
  public DockerClientException(String msg) {
    super(msg);
  }
  
  public DockerClientException(String msg, Exception e) {
    super(msg, e);
  }

}
