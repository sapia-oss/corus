package org.sapia.corus.cloud.platform.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.io.CharStreams;

import net.sf.json.JSONObject;

/**
 * Implements the {@link HttpClientFacade} interface on top of the JDK's {@link HttpURLConnection} class.
 * 
 * @author yduchesne
 *
 */
public class JdkHttpClientFacade implements HttpClientFacade {
  
  @Override
  public void close() {
    // noop
  }
  
  @Override
  public HttpResponse get(String url, long timeoutMillis, Map<String, String> headers) throws IOException {
    return new JdkHttpResponse(createHttpConnection(url, "GET", timeoutMillis, headers));
  }
  
  @Override
  public HttpResponse post(String url, long timeoutMillis, InputStream payload, Map<String, String> headers) throws IOException {
    return new JdkHttpResponse(createHttpConnection(url, "POST", timeoutMillis, headers));
  }
  
  @Override
  public HttpResponse put(String url, long timeoutMillis, InputStream payload, Map<String, String> headers) throws IOException {
    return new JdkHttpResponse(createHttpConnection(url, "PUT", timeoutMillis, headers));
  }
  
  // --------------------------------------------------------------------------
  // Restricted methods
  
  private HttpURLConnection createHttpConnection(String url, String method, long timeoutMillis, Map<String, String> headers) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setReadTimeout((int) timeoutMillis);
    conn.setConnectTimeout((int) timeoutMillis); 
    conn.setRequestMethod(method);
    conn.setAllowUserInteraction(true);
    conn.setDoOutput(method.equals("POST") || method.equals("PUT") );
    conn.setDoInput(true);
    
    for (Map.Entry<String, String> h : headers.entrySet()) {
      conn.setRequestProperty(h.getKey(), h.getValue());
    }
    
    return conn;
  }
  
  // --------------------------------------------------------------------------
  // Inner classes
  
  private class JdkHttpResponse implements HttpResponse {
  
    private HttpURLConnection connection;
    
    public JdkHttpResponse(HttpURLConnection connection) {
      this.connection = connection;
    }
    
    @Override
    public void close() {
      connection.disconnect();
    }
    
    @Override
    public int getStatusCode() {
      try {
        return connection.getResponseCode();
      } catch (IOException e) {
        throw new IllegalStateException("I/O error occurred while trying to obtain status code from response", e);
      }
    }
    
    @Override
    public String getStatusMessage() {
      try {
        return connection.getResponseMessage();
      } catch (IOException e) {
        throw new IllegalStateException("I/O error occurred while trying to obtain status message from response", e);
      }
    }
    
    @Override
    public Optional<String> getHeader(String name) {
      String value = connection.getHeaderField(name);
      if (value == null) {
        return Optional.absent();
      }
      return Optional.of(value);
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
      return connection.getInputStream();
    }
    
    @Override
    public JSONObject asJson() throws IOException {
      try (InputStream is = getInputStream()) {
        return JSONObject.fromObject(CharStreams.toString(new InputStreamReader(getInputStream())));
      }
    }
  }

}
