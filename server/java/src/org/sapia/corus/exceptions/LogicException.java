package org.sapia.corus.exceptions;

/**
 * @author Yanick Duchesne
 */
public class LogicException extends CorusException {
  
  static final long serialVersionUID = 1L;

  /**
   * Constructor for LogicException.
   * @param msg
   */
  public LogicException(String msg) {
    super(msg);
  }

  /**
   * Constructor for LogicException.
   * @param msg
   * @param err
   */
  public LogicException(String msg, Throwable err) {
    super(msg, err);
  }

  /**
   * Constructor for LogicException.
   * @param err
   */
  public LogicException(Throwable err) {
    super(err);
  }
}
