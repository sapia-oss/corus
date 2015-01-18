package org.sapia.corus.client.rest;

/**
 * Thrown to signal that a REST resource has not been found.
 * 
 * @author yduchesne
 *
 */
public class ResourceNotFoundException extends RuntimeException {
  
  static final long serialVersionUID = 1L;

  public ResourceNotFoundException(String msg) {
    super(msg);
  }
  
}
