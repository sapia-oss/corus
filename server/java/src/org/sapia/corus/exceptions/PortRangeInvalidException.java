package org.sapia.corus.exceptions;

/**
 * Thrown when port range is invalid.
 * 
 * @author yduchesne
 */
public class PortRangeInvalidException extends Exception{

  static final long serialVersionUID = 1L;

  /** Creates a new instance of PortRangeInvalidException */
  public PortRangeInvalidException(String msg) {
    super(msg);
  }
  
}
