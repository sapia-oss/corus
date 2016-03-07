package org.sapia.corus.cloud.platform.http;

/**
 * Factory class for obtaining {@link HttpClientFacade} instances.
 * 
 * @author yduchesne
 *
 */
public class JdkHttpClientFactory implements HttpClientFactory {
  
  @Override
  public HttpClientFacade getClient() {
    return new JdkHttpClientFacade();
  }

}
