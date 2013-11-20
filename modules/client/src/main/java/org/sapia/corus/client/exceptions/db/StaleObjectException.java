package org.sapia.corus.client.exceptions.db;

import org.sapia.corus.client.exceptions.CorusRuntimeException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * Thrown as part of the optimistic locking strategy, when a the version of an
 * object as changed.
 * 
 * @author yduchesne
 * 
 */
public class StaleObjectException extends CorusRuntimeException {

  static final long serialVersionUID = 1L;

  public StaleObjectException(String msg) {
    super(msg, ExceptionCode.STALE_DATA.getFullCode());
  }

}
