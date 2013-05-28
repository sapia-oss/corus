package org.sapia.corus.taskmanager.util;

import org.sapia.corus.taskmanager.core.ThrottleKey;
import org.sapia.corus.taskmanager.core.Throttleable;

/**
 * A {@link RunnableTask} that is also {@link Throttleable}.
 * 
 * @author yduchesne
 */
public abstract class RunnableThrottleableTask extends RunnableTask implements Throttleable {
  
  private ThrottleKey key;
  
  /**
   * @param key the {@link ThrottleKey} that this instance is meant to correspond to. 
   */
  public RunnableThrottleableTask(ThrottleKey key) {
    this.key = key;
  }

  @Override
  public ThrottleKey getThrottleKey() {
    return key;
  }
  
}
