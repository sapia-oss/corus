package org.sapia.corus.client.services.database.persistence;

public class FieldAccessException extends RuntimeException {

  static final long serialVersionUID = 1L;

  public FieldAccessException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
