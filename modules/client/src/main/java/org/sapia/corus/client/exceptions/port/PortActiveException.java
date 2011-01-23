package org.sapia.corus.client.exceptions.port;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;
import org.sapia.corus.client.services.port.PortManager;

/**
 * Thrown when a port is active - thus cannot be removed.
 * 
 * @see PortManager
 * 
 * @author yduchesne
 */
public class PortActiveException extends CorusException{
  
  static final long serialVersionUID = 1L;
  
  /** Creates a new instance of PortActiveException */
  public PortActiveException(String msg) {
    super(msg, ExceptionCode.PORT_ACTIVE.getFullCode());
  }
  
}
