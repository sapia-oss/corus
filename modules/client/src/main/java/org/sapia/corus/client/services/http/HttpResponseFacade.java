package org.sapia.corus.client.services.http;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Hides HTTP response implementation details.
 * 
 * @author yduchesne
 *
 */
public interface HttpResponseFacade {
  
  public static final int STATUS_OK           = 200;
  public static final int STATUS_NOT_FOUND    = 404;
  public static final int STATUS_SERVER_ERROR = 500;

  /**
   * @param name a header name.
   * @return the header's value, or <code>null</code> if there's no such header.
   */
  public String getHeader(String name);
  
  /**
   * @param code a status code.
   */
  public void setStatusCode(int code);
  
  /**
   * @param name a header name.
   * @param value a header value.
   */
  public void setHeader(String name, String value);
  
  /**
   * @param len a content length.
   */
  public void setContentLength(int len);
  
  /**
   * @return this instance's {@link OutputStream}.
   * @throws IOException if an IO error occurs.
   */
  public OutputStream getOutputStream() throws IOException;
  
  /**
   * Commits this instance.
   * @throws IOException if an IO error occurs. 
   */
  public void commit() throws IOException;

}

