package org.sapia.corus.client.services.database.persistence;

public class NoSuchFieldException extends RuntimeException {

  static final long serialVersionUID = 1L;

  public NoSuchFieldException(String msg) {
    super(msg);
  }

}
