package org.sapia.corus.client.exceptions.port;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;
import org.sapia.corus.client.services.port.PortManager;

/**
 * Thrown when a port range conflicts with an existing one.
 * 
 * @see PortManager
 * 
 * @author yduchesne
 */
public class PortRangeConflictException extends CorusException{
  
  static final long serialVersionUID = 1L;

  /** Creates a new instance of PortRangeConflictException */
  public PortRangeConflictException(String msg) {
    super(msg, ExceptionCode.PORT_RANGE_CONFLICT.getFullCode());
  }
  
}
