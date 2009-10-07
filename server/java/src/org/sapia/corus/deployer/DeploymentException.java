package org.sapia.corus.deployer;

import org.sapia.corus.exceptions.CorusException;


/**
 * Signals that a problem occurred while deploying.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DeploymentException extends CorusException {
  /**
   * Constructor for DeploymentException.
   * @param msg
   */
  public DeploymentException(String msg) {
    super(msg);
  }

  /**
   * Constructor for DeploymentException.
   * @param msg
   * @param err
   */
  public DeploymentException(String msg, Throwable err) {
    super(msg, err);
  }

  /**
   * Constructor for DeploymentException.
   * @param err
   */
  public DeploymentException(Throwable err) {
    super(err);
  }
}
