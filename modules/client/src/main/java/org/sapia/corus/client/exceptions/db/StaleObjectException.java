package org.sapia.corus.client.exceptions.db;

import org.sapia.corus.client.exceptions.CorusRuntimeException;
import org.sapia.corus.client.exceptions.ExceptionCode;

public class StaleObjectException extends CorusRuntimeException{
  
  static final long serialVersionUID = 1L;
  
  public StaleObjectException(String msg) {
    super(msg, ExceptionCode.STALE_DATA.getFullCode());
  }

}
