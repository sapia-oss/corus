package org.sapia.corus.taskmanager.core;

/**
 * Marks {@link Task}s whose instances should be executed in the 
 * context of a {@link Throttle}.
 * 
 * @author yduchesne
 *
 * @param <K>
 */
public interface Throttleable{
  
  /**
   * @return this instance's {@link ThrottleKey}.
   */
  public ThrottleKey getThrottleKey();

}
