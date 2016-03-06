package org.sapia.corus.cloud.platform.http;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.base.Optional;

import net.sf.json.JSONObject;

/**
 * Specifies HTTP response behavior.
 * 
 * @author yduchesne
 *
 */
public interface HttpResponse {
  
  int STATUS_OK                 = 200;
  int STATUS_SERVER_ERROR       = 500;
  int STATUS_DIAGNOSTIC_PENDING = 503;

  /**
   * @return the response's status code.
   */
  public int getStatusCode();
  
  /**
   * @return the response's status message.
   */
  public String getStatusMessage();
  
  /**
   * @param name the name of the header whose value should be returned.
   * @return the {@link Optional} header value (will be empty if not value was
   * found for the corresponding header).
   */
  public Optional<String> getHeader(String name);
  
  /**
   * @return the response's stream.
   * @throws IOException if an I/O error occurs.
   */
  public InputStream getInputStream() throws IOException;
  
  /**
   * @return the response payload, as a {@link JSONObject}.
   * @throws IOException if an I/O error occurs.
   */
  public JSONObject asJson() throws IOException;
  
  /**
   * Performs internal cleanup if required.
   */
  public void close();
  
}
