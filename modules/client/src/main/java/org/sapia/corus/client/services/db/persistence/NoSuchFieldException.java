package org.sapia.corus.client.services.db.persistence;

public class NoSuchFieldException extends RuntimeException {

  static final long serialVersionUID = 1L;

  public NoSuchFieldException(String msg) {
    super(msg);
  }

}
