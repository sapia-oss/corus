package org.sapia.corus.cloud.platform.workflow.exceptions;

/**
 * Thrown when a deployment is aborted, due to an irrecoverable error occurring.
 * 
 * @author yduchesne
 */
public class AbortedDeploymentException extends IllegalStateException {
  
  public AbortedDeploymentException(String msg) {
    super(msg);
  }
  
  public AbortedDeploymentException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
