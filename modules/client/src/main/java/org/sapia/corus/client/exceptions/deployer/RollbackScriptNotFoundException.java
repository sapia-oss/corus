package org.sapia.corus.client.exceptions.deployer;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * Signals that a given distribution does not have a rollback script.
 * 
 * @author Yanick Duchesne
 */
public class RollbackScriptNotFoundException extends CorusException {

  static final long serialVersionUID = 1L;

  public RollbackScriptNotFoundException(String msg) {
    super(msg, ExceptionCode.NO_ROLLBACK_SCRIPT.getFullCode());
  }
}
