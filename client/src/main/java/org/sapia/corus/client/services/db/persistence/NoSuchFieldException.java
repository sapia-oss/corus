package org.sapia.corus.client.services.db.persistence;

public class NoSuchFieldException extends RuntimeException{
  
  public NoSuchFieldException(String msg) {
    super(msg);
  }

}
