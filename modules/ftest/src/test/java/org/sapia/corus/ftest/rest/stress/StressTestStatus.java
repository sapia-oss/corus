package org.sapia.corus.ftest.rest.stress;

import org.sapia.ubik.util.TimeValue;

public class StressTestStatus {
  
  private TimeValue elapsed;
  private int       totalRunCount;
  
  public StressTestStatus(TimeValue elapsed, int totalRunCount) {
    this.elapsed       = elapsed;
    this.totalRunCount = totalRunCount;
  }
  
  public int getTotalRunCount() {
    return totalRunCount;
  }
  
  public TimeValue getElapsed() {
    return elapsed;
  }
  
  @Override
  public String toString() {
    return String.format("Completed %s runs in %s seconds", totalRunCount, elapsed);
  }
  
}
