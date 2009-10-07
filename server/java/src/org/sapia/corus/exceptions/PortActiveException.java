package org.sapia.corus.exceptions;

import org.sapia.corus.admin.services.port.PortManager;

/**
 * Thrown when a port is active - thus cannot be removed.
 * 
 * @see PortManager
 * 
 * @author yduchesne
 */
public class PortActiveException extends Exception{
  
  static final long serialVersionUID = 1L;
  
  /** Creates a new instance of PortActiveException */
  public PortActiveException(String msg) {
    super(msg);
  }
  
}
