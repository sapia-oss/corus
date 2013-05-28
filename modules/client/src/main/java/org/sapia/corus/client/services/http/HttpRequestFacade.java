package org.sapia.corus.client.services.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * Hides HTTP reqiest implementation details.
 * 
 * @author yduchesne
 *
 */
public interface HttpRequestFacade {

  /**
   * @param name a header name.
   * @return the header's value, or <code>null</code> if there's no such header.
   */
  public String getHeader(String name);

  /**
   * @param name a parameter name.
   * @return the parameter's value, or <code>null</code> if there's no such value.
   */  
  public String getParameter(String name);
  
  /**
   * @return this instance's {@link InputStream}.
   * @throws IOException if an IO error occurs.
   */
  public InputStream getInputStream() throws IOException;

}
