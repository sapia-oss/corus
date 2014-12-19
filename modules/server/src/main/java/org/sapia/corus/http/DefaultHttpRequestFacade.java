package org.sapia.corus.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.client.services.http.HttpRequestFacade;
import org.simpleframework.http.Request;

/**
 * Implements the {@link HttpRequestFacade} around the {@link Request}
 * interface.
 * 
 * @author yduchesne
 * 
 */
public class DefaultHttpRequestFacade implements HttpRequestFacade {

  private Request request;

  public DefaultHttpRequestFacade(Request request) {
    this.request = request;
  }

  @Override
  public String getParameter(String name) {
    return request.getParameter(name);
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public Map<String, String> getParameters() {
    Map<String, String> toReturn = new HashMap<String, String>();
    Map attributes = request.getAttributes();
    for (Object k : attributes.keySet()) {
      if (k instanceof String) {
        Object v = attributes.get((String) k);
        if (v instanceof String) {
          toReturn.put((String) k, (String) v); 
        }
      }
    }
    return toReturn;
  }

  @Override
  public String getHeader(String name) {
    return request.getValue(name);
  }
  
  @Override
  public Set<String> getAccepts() {
    return new HashSet<String>(request.getValues("Accept"));
  }
  
  @Override
  public String getContentType() {
    return request.getContentType() == null ? null : request.getContentType().getType();
  }
  
  @Override
  public String getMethod() {
    return request.getMethod();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return request.getInputStream();
  }

}
