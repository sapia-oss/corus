package org.sapia.corus.client.rest;

import org.sapia.corus.client.facade.CorusConnector;

/**
 * Specifies pooling behavior of {@link CorusConnector}s.
 * 
 * @author yduchesne
 *
 */
public interface ConnectorPool {

  /**
   * @return a pooled {@link CorusConnector}.
   */
  public CorusConnector acquire();
  
  /**
   * 
   * @param previously acquired {@link CorusConnector}.
   */
  public void release(CorusConnector release);
}
