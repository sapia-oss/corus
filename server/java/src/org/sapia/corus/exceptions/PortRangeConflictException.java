package org.sapia.corus.exceptions;

import org.sapia.corus.admin.services.port.PortManager;

/**
 * Thrown when a port range conflicts with an existing one.
 * 
 * @see PortManager
 * @author yduchesne
 */
public class PortRangeConflictException extends Exception{
  
  static final long serialVersionUID = 1L;

  /** Creates a new instance of PortRangeConflictException */
  public PortRangeConflictException(String msg) {
    super(msg);
  }
  
}
