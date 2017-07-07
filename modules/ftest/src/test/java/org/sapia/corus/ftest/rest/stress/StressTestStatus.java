package org.sapia.corus.ftest.rest.stress;

import org.sapia.ubik.util.TimeValue;

/**
 * Returned by a {@link StressTestTask}.
 * 
 * @author yduchesne
 *
 */
public class StressTestStatus {
  
  private TimeValue elapsed;
  private int       totalRunCount;

  /**
   * @param elapsed       a {@link TimeValue} corresponding to the amount of time taken by the {@link StressTestTask}
   *                      to perform its work.
   * @param totalRunCount the total number of "runs" that the {@link StressTestTask} performed.
   */
  public StressTestStatus(TimeValue elapsed, int totalRunCount) {
    this.elapsed       = elapsed;
    this.totalRunCount = totalRunCount;
  }
  
  /**
   * @return the total number of "runs" that the {@link StressTestTask} performed.
   */
  public int getTotalRunCount() {
    return totalRunCount;
  }
  
  /**
   * @return tje {@link TimeValue} corresponding to the amount of time that the {@link StressTestTask} took.
   */
  public TimeValue getElapsed() {
    return elapsed;
  }
  
  @Override
  public String toString() {
    return String.format("Completed %s runs in %s seconds", totalRunCount, elapsed);
  }
  
}
