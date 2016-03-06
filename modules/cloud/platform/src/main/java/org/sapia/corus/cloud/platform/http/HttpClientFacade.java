package org.sapia.corus.cloud.platform.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface HttpClientFacade {
  
  public HttpResponse get(String url, long timeoutMillis, Map<String, String> headers) throws IOException;
  
  public HttpResponse post(String url, long timeoutMillis, InputStream payload, Map<String, String> headers) throws IOException;

  public HttpResponse put(String url, long timeoutMillis, InputStream payload, Map<String, String> headers) throws IOException;

  public void close();

}
