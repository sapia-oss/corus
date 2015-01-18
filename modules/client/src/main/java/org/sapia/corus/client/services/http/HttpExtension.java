package org.sapia.corus.client.services.http;

import java.io.FileNotFoundException;

/**
 * Specifies the behavior of a so-called HTTP extension, that can be plugged
 * into the {@link HttpModule}.
 * 
 * @author yduchesne
 * 
 */
public interface HttpExtension {
  
  public static final String CORUS_HEADER_APP_ID  = "X-corus-app-id";
  public static final String CORUS_HEADER_APP_KEY = "X-corus-app-key";
  public static final String CORUS_PARAM_APP_ID   = "aid";
  public static final String CORUS_PARAM_APP_KEY  = "apk";

  /**
   * @return the {@link HttpExtensionInfo} holding this instance's information.
   */
  public HttpExtensionInfo getInfo();

  /**
   * This method processes the request corresponding to the passed in HTTP
   * context.
   * 
   * @param ctx
   *          a {@link HttpContext}
   * @throws Exception
   */
  public void process(HttpContext ctx) throws Exception, FileNotFoundException;
}
