package org.sapia.corus.util;

import java.util.Random;

/**
 * Holds time-related utility methods.
 * 
 * @author yduchesne
 *
 */
public final class TimeUtil {
  
  private TimeUtil() {
  }
  
  /**
   * @param minTimeMillis the minimal delay, in millis.
   * @param maxIntervalMillis the max time interval, in millis.
   * @return a random delay, in millis, corresponding to <code>minTimeMillis</code> +
   * <code>maxIntervalMillis</code>.
   */
  public static long createRandomDelay(long minTimeMillis, int maxIntervalMillis)  {
    return minTimeMillis += new Random().nextInt(maxIntervalMillis);
  }
}
