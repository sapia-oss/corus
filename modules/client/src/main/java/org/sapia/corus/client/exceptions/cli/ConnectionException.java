package org.sapia.corus.client.exceptions.cli;

import org.sapia.corus.client.exceptions.CorusRuntimeException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * Thrown when a connection to the Corus server could not be obtained.
 * 
 * @author yduchesne
 *
 */
public class ConnectionException extends CorusRuntimeException{
  
  static final long serialVersionUID = 1L;
  
  public ConnectionException(String msg, Throwable err) {
    super(msg, ExceptionCode.CONNECTION_ERROR.getFullCode(), err);
  }

}
