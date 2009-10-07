package org.sapia.corus.exceptions;

import org.sapia.corus.util.NestedException;


/**
 * The base Corus exception.
 * 
 * @author Yanick Duchesne
 */
public class CorusException extends Exception {
  
  static final long serialVersionUID = 1L;
  
  public CorusException(String msg) {
    super(msg);
  }

  public CorusException(String msg, Throwable err) {
    super(msg, err);
  }

  public CorusException(Throwable err) {
    super(err);
  }
}
