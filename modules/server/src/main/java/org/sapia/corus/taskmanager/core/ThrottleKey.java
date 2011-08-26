package org.sapia.corus.taskmanager.core;

/**
 * An instance of this interface maps to a {@link Throttle} instance. Implementations of this interface must implement {@link #hashCode()} and
 * {@link #equals(Object)} so that their instance will be distinguishable from others.  
 * 
 * @author yduchesne
 *
 */
public interface ThrottleKey {

  /**
   * @return the name of this instance.
   */
  public String getName();
}
