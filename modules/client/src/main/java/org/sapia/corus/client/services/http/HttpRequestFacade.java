package org.sapia.corus.client.services.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

/**
 * Hides HTTP request implementation details.
 * 
 * @author yduchesne
 * 
 */
public interface HttpRequestFacade {
  
  /**
   * @return the address of the hos from which this request originates.
   */
  public String getRemoteHost();
  
  /**
   * @return the HTTP method name (POST, GET, etc.).
   */
  public String getMethod();
  
  /**
   * @return the {@link Set} of mime-types corresponding to the different content types
   * accepted by the client.
   */
  public Set<String> getAccepts();
  
  /**
   * @return the mime-type of the request's payload, or <code>null</code> if none
   * has been specified, or if this does not apply (for example in the case of a GET method).
   */
  public String getContentType();

  /**
   * @param name
   *          a header name.
   * @return the header's value, or <code>null</code> if there's no such header.
   */
  public String getHeader(String name);

  /**
   * @param name
   *          a parameter name.
   * @return the parameter's value, or <code>null</code> if there's no such
   *         value.
   */
  public String getParameter(String name);
  
  /**
   * @return the {@link Map} of name/values corresponding to the request's parameters.
   */
  public Map<String, String> getParameters();
  
  /**
   * @return this instance's {@link InputStream}.
   * @throws IOException
   *           if an IO error occurs.
   */
  public InputStream getInputStream() throws IOException;

}
