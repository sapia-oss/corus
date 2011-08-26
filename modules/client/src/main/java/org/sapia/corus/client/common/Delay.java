package org.sapia.corus.client.common;

import java.util.concurrent.TimeUnit;

/**
 * Models a delay. An instance is used to determine if a given
 * time delay has been reached. 
 * <p>
 * Usage:
 * <p>
 * <pre>
 * Delay seconds = new Delay(10, TimeUnit.SECONDS);
 * Thread.sleep(10000);
 * System.out.println(seconds.isOver());
 * </pre>
 * 
 * @author yduchesne
 *
 */
public class Delay {
  
  /**
   * This instance's start time.
   */
  private long start;
  
  /**
   * This instance's duration, in millis.
   */
  private long durationMillis;
  
  public Delay(long duration, TimeUnit timeUnit) {
    this.durationMillis = TimeUnit.MILLISECONDS.convert(duration, timeUnit);
  }
  
  /**
   * Sets this instance's internal start time to <code>System.currentTimeMillis()</code>.
   * 
   * @return this instance.
   */
  public Delay start(){
    start = System.currentTimeMillis();
    return this;
  }
  
  /**
   * @return <code>true</code> if the time interval since 
   * this instance's start time is greater than or equal to 
   * this instance's duration.
   */
  public boolean isOver(){
    return System.currentTimeMillis() - start >= durationMillis;
  }
  
  /**
   * @return <code>true</code> if the delay is not over.
   * @see #isOver()
   */
  public boolean isNotOver(){
    return !isOver();
  }
  
  
  /**
   * @return the time remaining to this delay, in millis.
   */
  public long remainingMillis(){
    long remaining = durationMillis - (System.currentTimeMillis() - start);
    return remaining < 0 ? 0 : remaining;
  }
}
