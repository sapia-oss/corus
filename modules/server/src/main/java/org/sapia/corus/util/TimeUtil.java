package org.sapia.corus.util;

import java.util.Random;

import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.TimeRange;

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
   * @param minTimeMillis
   *          the minimal delay, in millis.
   * @param maxTimeMillis
   *          the max time interval, in millis.
   * @return a random delay, in millis, corresponding to
   *         <code>minTimeMillis + random(maxTimeMillis - minTimeMillis)</code>.
   */
  public static long createRandomDelay(long minTimeMillis, long maxTimeMillis) {
    Assertions.isTrue(maxTimeMillis > minTimeMillis, "Max time must greater than min time (got %s vs %s)", maxTimeMillis, minTimeMillis);
    return minTimeMillis + new Random().nextInt((int) (maxTimeMillis - minTimeMillis));
  }
  
  /**
   * @param range a {@link TimeRange}.
   * @return a random number of milliseconds within the given time range.
   */
  public static long createRandomDelay(TimeRange range) {
    return createRandomDelay(range.getMin().getValueInMillis(), range.getMax().getValueInMillis());
  }
}
