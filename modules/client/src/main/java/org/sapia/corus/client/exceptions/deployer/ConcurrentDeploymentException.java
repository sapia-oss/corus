package org.sapia.corus.client.exceptions.deployer;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * This exception signals that two concurrent deployments have occurred.
 * 
 * @author Yanick Duchesne
 */
public class ConcurrentDeploymentException extends CorusException {

  static final long serialVersionUID = 1L;

  public ConcurrentDeploymentException(String msg) {
    super(msg, ExceptionCode.CONCURRENT_DEPLOYMENT.getFullCode());
  }
}
