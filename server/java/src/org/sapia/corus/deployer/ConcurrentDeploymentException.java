package org.sapia.corus.deployer;

import org.sapia.corus.exceptions.CorusException;


/**
 * This exception signals that two concurrent deployments have occurred.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ConcurrentDeploymentException extends CorusException {
  /**
   * Constructor for ConcurrentDeploymentException.
   * @param msg
   */
  public ConcurrentDeploymentException(String msg) {
    super(msg);
  }

  /**
   * Constructor for ConcurrentDeploymentException.
   * @param msg
   * @param err
   */
  public ConcurrentDeploymentException(String msg, Throwable err) {
    super(msg, err);
  }

  /**
   * Constructor for ConcurrentDeploymentException.
   * @param err
   */
  public ConcurrentDeploymentException(Throwable err) {
    super(err);
  }
}
