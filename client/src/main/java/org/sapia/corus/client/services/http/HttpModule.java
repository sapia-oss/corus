package org.sapia.corus.client.services.http;

import org.sapia.corus.client.Module;


/**
 * This module handles incoming HTTP requests from polling distributed VMs.
 *
 * @author Yanick Duchesne
 */
public interface HttpModule extends Module{

  
  public static String ROLE = HttpModule.class.getName();
  
  /***
   * Adds a {@link HttpExtension} to this instance.
   * 
   * @param ext a {@link HttpExtension}
   */
  public void addHttpExtension(HttpExtension ext);
  
}
