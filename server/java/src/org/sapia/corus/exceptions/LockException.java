package org.sapia.corus.exceptions;

/**
 * Indicates that the lock on a given process could not be obtained.
 * 
 * @author Yanick Duchesne
 */
public class LockException extends Exception {
  
  static final long serialVersionUID = 1L;

  public LockException(String msg) {
    super(msg);
  }
}
