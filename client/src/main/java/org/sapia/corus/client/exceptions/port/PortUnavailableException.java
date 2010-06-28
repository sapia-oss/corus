package org.sapia.corus.client.exceptions.port;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;
import org.sapia.corus.client.services.port.PortManager;

/**
 * Thrown when a port is unavailable.
 * 
 * @see PortManager
 * @author yduchesne
 */
public class PortUnavailableException extends CorusException{
  
  static final long serialVersionUID = 1L;

  /** Creates a new instance of PortUnavailableException */
  public PortUnavailableException(String msg) {
    super(msg, ExceptionCode.PORT_UNAVAILABLE.getFullCode());
  }
  
}
