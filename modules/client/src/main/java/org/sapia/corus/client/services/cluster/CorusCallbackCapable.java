package org.sapia.corus.client.services.cluster;

import org.sapia.ubik.rmi.server.command.InvokeCommand;

/**
 * Interfaces to be implemented by {@link InvokeCommand}s that need a reference to Corus server-side
 * objects at execution time. 
 * 
 * @author yduchesne
 *
 */
public interface CorusCallbackCapable {
  
  /**
   * @param callback a {@link CorusCallback}.
   */
  public void setCorusCallback(CorusCallback callback);

}
