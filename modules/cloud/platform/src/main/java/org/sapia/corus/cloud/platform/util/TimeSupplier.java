package org.sapia.corus.cloud.platform.util;

import java.util.concurrent.TimeUnit;

/**
 * Abstracts generation of time values, mainly to allow for deterministic testing.
 * 
 * @author yduchesne
 *
 */
public interface TimeSupplier {
  
  /**
   * @return the current time, in millis.
   */
  public long currentTimeMillis();
  
  /**
   * @return the current time, in nanos.
   */
  public long currentTimeNanos();

  // ==========================================================================
  
  /**
   * A {@link TimeSupplier} implementation using {@link System#currentTimeMillis()} and {@link System#nanoTime()}.
   * 
   * @author yduchesne
   *
   */
  public static class SystemTime implements TimeSupplier {
    
    private static final SystemTime INSTANCE = new SystemTime();
    
    @Override
    public long currentTimeMillis() {
      return System.currentTimeMillis();
    }
    
    @Override
    public long currentTimeNanos() {
      return System.nanoTime();
    }
    
    /**
     * @return the singleton instance of this class.
     */
    public static SystemTime getInstance() {
      return INSTANCE;
    }
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * A new instance of this class, which provides a mutable time value (and 
   * corresponding mutator methods). Mainly intended for use in the context of
   * unit testing.
   * 
   * @author yduchesne
   *
   */
  public static class MutableTime implements TimeSupplier {
    
    private long timeValue;
    
    public MutableTime addTime(long timeMillis) {
      this.timeValue = timeMillis;
      return this;
    }
    
    public MutableTime addTime(TimeMeasure time) {
      this.timeValue += time.getMillis();
      return this;
    }
    
    public MutableTime setTime(long timeMillis) {
      this.timeValue = timeMillis;
      return this;
    }
    
    public MutableTime setTime(TimeMeasure time) {
      this.timeValue = time.getMillis();
      return this;
    }
    
    @Override
    public long currentTimeMillis() {
      return timeValue;
    }
    
    public long currentTimeNanos() {
      return TimeUnit.NANOSECONDS.convert(timeValue, TimeUnit.MILLISECONDS);
    }
    
    /**
     * @return a new instance of this class.
     */
    public static MutableTime getInstance() {
      return new MutableTime();
    }
  }
  
}
