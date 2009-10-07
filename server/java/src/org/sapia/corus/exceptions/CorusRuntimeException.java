package org.sapia.corus.exceptions;

/**
 * The base Corus runtime exception.
 *
 * @author Yanick Duchesne
 */
public class CorusRuntimeException extends RuntimeException {
  
  static final long serialVersionUID = 1L;
  
  public CorusRuntimeException(String msg) {
    super(msg);
  }

  public CorusRuntimeException(String msg, Throwable err) {
    super(msg, err);
  }

  public CorusRuntimeException(Throwable err) {
    super(err);
  }
}
