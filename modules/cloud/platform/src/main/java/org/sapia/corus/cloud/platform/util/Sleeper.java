package org.sapia.corus.cloud.platform.util;

/**
 * Abstract the mechanism used for putting threads to sleep.
 * 
 * @author yanick
 *
 */
public interface Sleeper {
  
  /**
   * @param duration a sleep duration, in millis.
   * @throws InterruptedException if the calling thread is interrupted while sleeping.
   */
  public void sleep(long duration) throws InterruptedException;
  
  // ==========================================================================
  // Implementations
  
  class SystemSleeper implements Sleeper {
    
    private static final Sleeper INSTANCE = new SystemSleeper();
    
    public void sleep(long duration) throws InterruptedException {
      Thread.sleep(duration);
    }
    
    public static Sleeper getInstance() {
      return INSTANCE;
    }
  }
  
  class NullSleeper implements Sleeper {
    
    private static final Sleeper INSTANCE = new NullSleeper();
    
    public void sleep(long duration) throws InterruptedException {
      // noop
    }
    
    public static Sleeper getInstance() {
      return INSTANCE;
    }
  }

}
