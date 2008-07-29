package org.sapia.corus.deployer;

import org.sapia.corus.LogicException;


/**
 * Signals that a distribution with a given name and version has already been deployed.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DuplicateDistributionException extends LogicException {
  /**
   * Constructor for DuplicateDistributionException.
   * @param msg
   */
  public DuplicateDistributionException(String msg) {
    super(msg);
  }

  /**
   * Constructor for DuplicateDistributionException.
   * @param msg
   * @param err
   */
  public DuplicateDistributionException(String msg, Throwable err) {
    super(msg, err);
  }

  /**
   * Constructor for DuplicateDistributionException.
   * @param err
   */
  public DuplicateDistributionException(Throwable err) {
    super(err);
  }
}
