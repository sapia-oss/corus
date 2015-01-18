package org.sapia.corus.client.rest;

/**
 * Hides HTTP response details.
 * 
 * @author yduchesne
 *
 */
public interface RestResponseFacade {

  /**
   * @param contentType the response's content type.
   */
  public void setContentType(String contentType);
  
  /**
   * @param statusCode the response's status code.
   */
  public void setStatus(int statusCode);
  
  /**
   * @param msg the response's status message.
   */
  public void setStatusMessage(String msg);
  
}
