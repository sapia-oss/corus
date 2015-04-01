package org.sapia.corus.cloud.topology;

/**
 * Implemented by classes whose instances can validate the data they hold.
 * 
 * @author yduchesne
 *
 */
public interface Validateable {

  /**
   * @throws IllegalArgumentException if invalid data is detected.
   */
  public void validate() throws IllegalArgumentException;
}
