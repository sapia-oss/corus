package org.sapia.corus.client.services.security;

/**
 * Thrown when security-related problem occurs - most likely when a security
 * rules have not been respected.
 * 
 * @author yduchesne
 */
public class CorusSecurityException extends RuntimeException {
  
  /**
   * Holds constants corresponding to the different possible types of security 
   * breaches.
   * 
   * @author yduchesne
   *
   */
  public enum Type {
    HOST_NOT_AUTHORIZED,
    OPERATION_NOT_AUTHORIZED,
    NO_SUCH_ROLE,
    INVALID_APP_ID_OR_KEY;
  }
  
  // --------------------------------------------------------------------------
  
  static final long serialVersionUID = 1L;

  private Type type;
  
  /**
   * @param msg an error message.
   * @param type the security exception type.
   */
  public CorusSecurityException(String msg, Type type) {
    super(msg);
    this.type = type;
  }
  
  /**
   * @return the security exception type.
   */
  public Type getType() {
    return type;
  }
  
}
