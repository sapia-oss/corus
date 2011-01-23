package org.sapia.corus.client.services;

/**
 * This interface specifies the methods common to all Corus internal services.
 * @author yduchesne
 *
 */
public interface Service {

  /**
   * This method is called after a service has been instantiated and its
   * properties have been set.
   * 
   * @throws Exception
   */
  public void init() throws Exception;

  /**
   * This method is called after a service has been initialized (after the {@link #init()} method
   * has been called on it)..
   * 
   * @throws Exception
   */
  public void start() throws Exception;
  
  /**
   * This method is called upon shutdown of the Corus server.
   * 
   * @throws Exception
   */
  public void dispose() throws Exception;
}
