package org.sapia.corus.client.exceptions.deployer;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * Signals that a given distribution cannot be undeployed because it has
 * processes currently running.
 * 
 * @author Yanick Duchesne
 */
public class RunningProcessesException extends CorusException {
  
  static final long serialVersionUID = 1L;
  
  /**
   * Constructor for DuplicateDistributionException.
   * @param msg
   */
  public RunningProcessesException(String msg) {
    super(msg, ExceptionCode.RUNNING_PROCESSES.getFullCode());
  }
}
 
