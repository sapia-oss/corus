package org.sapia.corus.client.exceptions.deployer;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * Signals that a distribution with a given name and version has already been deployed.
 * 
 * @author Yanick Duchesne
 */
public class DuplicateDistributionException extends CorusException {
  
  static final long serialVersionUID = 1L;
  
  /**
   * Constructor for DuplicateDistributionException.
   * @param msg
   */
  public DuplicateDistributionException(String msg) {
    super(msg, ExceptionCode.DUPLICATE_DISTRIBUTION.getFullCode());
  }
}
 
