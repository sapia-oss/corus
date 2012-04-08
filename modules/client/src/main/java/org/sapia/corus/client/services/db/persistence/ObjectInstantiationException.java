package org.sapia.corus.client.services.db.persistence;

public class ObjectInstantiationException extends RuntimeException{
	
	static final long serialVersionUID = 1L;
  
  public ObjectInstantiationException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
