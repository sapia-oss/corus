package org.sapia.corus.docker;

/**
 * Runtime exception for the {@link DockerFacade}.
 *
 * @author jcdesrochers
 */
public class DockerFacadeException extends RuntimeException {

  private static final long serialVersionUID = -5648545999431731467L;

  public DockerFacadeException(String message, Throwable cause) {
    super(message, cause);
  }

}
