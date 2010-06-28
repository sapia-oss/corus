package org.sapia.corus.client.exceptions.deployer;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * Thrown when a given distribution cannot be found.
 * 
 * @author yduchesne
 *
 */
public class DistributionNotFoundException extends CorusException{
  
  static final long serialVersionUID = 1L;
  
  public DistributionNotFoundException(String msg) {
    super(msg, ExceptionCode.DISTRIBUTION_NOT_FOUND.getFullCode());
  }

}
