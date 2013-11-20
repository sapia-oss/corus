package org.sapia.corus.client.common;

/**
 * Indicates that an error occured while processing the content of a
 * {@link ProgressQueue}.
 * 
 * @author yduchesne
 * 
 */
public class ProgressException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ProgressException(String msg) {
    super(msg);
  }

  public ProgressException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
