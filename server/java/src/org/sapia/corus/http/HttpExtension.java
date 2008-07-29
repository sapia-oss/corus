package org.sapia.corus.http;

import java.io.FileNotFoundException;

/**
 * Specifies the behavior of a so-called HTTP extension, that
 * can be plugged into the HTTP Module.
 * 
 * @
 * @author yduchesne
 *
 */
public interface HttpExtension {
  
  /**
   * @return the <code>HttpExtensionInfo</code> holding this 
   * instance's information.
   */
  public HttpExtensionInfo getInfo();

  /**
   * This method processes the request corresponding to the
   * passed in HTTP context.
   * 
   * @param ctx a <code>HTTPContext</code>
   * @throws Exception
   */
  public void process(HttpContext ctx) 
    throws Exception, FileNotFoundException;
}
