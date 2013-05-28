package org.sapia.corus.client.services.repository;



/**
 * This interface specifies the behavior of the Repository module.
 * 
 * @author yduchesne
 *
 */
public interface Repository {
  
  /**
   * The module's role constant.
   */
  public static final String ROLE = Repository.class.getName();
  
  /**
   * Forces a pull from repository server nodes.
   */
  public void pull();
  
  /**
   * Forces a push to repository client nodes.
   */
  public void push();
}
