package org.sapia.corus.client.exceptions.deployer;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * Thrown when a deployment error occurs.
 * 
 * @author Yanick Duchesne
 */
public class DeploymentException extends CorusException {

  static final long serialVersionUID = 1L;

  public DeploymentException(String msg) {
    super(msg, ExceptionCode.DEPLOYMENT_ERROR.getFullCode());
  }

  public DeploymentException(String msg, Throwable err) {
    super(msg, ExceptionCode.DEPLOYMENT_ERROR.getFullCode(), err);
  }
}
