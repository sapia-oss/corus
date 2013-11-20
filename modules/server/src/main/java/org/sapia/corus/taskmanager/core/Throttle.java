package org.sapia.corus.taskmanager.core;

/**
 * This interface specifies the behavior common to all throttle implementations.
 * 
 * @author yduchesne
 * 
 */
public interface Throttle {

  /**
   * Executes the given {@link Runnable} in a discretionnary manner.
   * 
   * @param toRun
   *          a {@link Runnable}.
   */
  public void execute(Runnable toRun);
}
