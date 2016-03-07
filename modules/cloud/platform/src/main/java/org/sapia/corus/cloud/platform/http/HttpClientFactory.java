package org.sapia.corus.cloud.platform.http;

/**
 * Specifies the behavior for factories of {@link HttpClientFacade}.
 * 
 * @author yduchesne
 * 
 */
public interface HttpClientFactory {

  /**
   * @return a new {@link HttpClientFacade}.
   */
  public HttpClientFacade getClient();
}
