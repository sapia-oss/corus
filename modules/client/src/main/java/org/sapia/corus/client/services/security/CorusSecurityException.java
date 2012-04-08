package org.sapia.corus.client.services.security;

/**
 * Thrown when security-related problem occurs - most likely when a security rules
 * have not been respected..
 * 
 * @author yduchesne
 */
public class CorusSecurityException extends RuntimeException{
  
	static final long serialVersionUID = 1L;
  
	public CorusSecurityException(String msg){
    super(msg);
  }
}
