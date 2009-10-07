package org.sapia.corus.exceptions;

import org.sapia.corus.admin.services.port.PortManager;

/**
 * Thrown when a port is unavailable.
 * 
 * @see PortManager
 * @author yduchesne
 */
public class PortUnavailableException extends Exception{
  
  static final long serialVersionUID = 1L;

  /** Creates a new instance of PortUnavailableException */
  public PortUnavailableException(String msg) {
    super(msg);
  }
  
}
