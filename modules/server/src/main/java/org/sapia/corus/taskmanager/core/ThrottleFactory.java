package org.sapia.corus.taskmanager.core;

import java.util.concurrent.TimeUnit;

/**
 * This class implements a factory of {@link Throttle} instances.
 * 
 * @author yduchesne
 * 
 */
public class ThrottleFactory {

  /**
   * @param maxConcurrent
   *          the maximum number of concurrent execution.
   * @return a new {@link Throttle}.
   * @see SemaphoreThrottle.
   */
  public static Throttle createMaxConcurrentThrottle(int maxConcurrent) {
    return new SemaphoreThrottle(maxConcurrent);
  }

  /**
   * @param unit
   *          the {@link TimeUnit} into which the given time interval is
   *          expressed.
   * @param interval
   *          the time interval between each execution.
   * @return a new {@link Throttle}.
   * @see TimeIntervalThrottle.
   */
  public static Throttle createTimeIntervalThrottle(TimeUnit unit, long interval) {
    return new TimeIntervalThrottle(unit, interval);
  }
}
