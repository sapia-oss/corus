package org.sapia.corus.cloud.platform.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Abstracts the source of an {@link InputStream}.
 * 
 * @author yduchesne
 *
 */
public interface Input {
  
  /**
   * @return information about this instance.
   */
  public String getInfo();
  
  /**
   * @return this instance's {@link InputStream}.
   * @throws IOException if an I/O problem occurs obtaining the underlying stream.
   */
  public InputStream getInputStream() throws IOException;

}
