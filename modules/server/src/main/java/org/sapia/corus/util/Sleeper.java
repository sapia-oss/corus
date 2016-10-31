package org.sapia.corus.util;

import org.sapia.ubik.util.SysClock.MutableClock;

/**
 * Meant to allow for mocking {@link Thread#sleep(long)} in the context of testing.
 * 
 * @author yduchesne
 *
 */
public interface Sleeper {
  
  /**
   * @param millis the number of milliseconds to sleep for.
   * @throws InterruptedException if the calling thread is interrupted while sleeping.
   */
  public void sleep(long millis) throws InterruptedException;
  
  // --------------------------------------------------------------------------
  // Built-in implementations.
  
  public static class ThreadSleeper implements Sleeper {
    
    private static final Sleeper INSTANCE = new ThreadSleeper();
    
    private ThreadSleeper() {
      
    }
    
    @Override
    public void sleep(long millis) throws InterruptedException {
      Thread.sleep(millis);
    }
    
    
    public static Sleeper getInstance() {
      return INSTANCE;
    }
    
  }
  
  
  public static class MockSleeper implements Sleeper {
    
    private MutableClock clock;
    
    public MockSleeper(MutableClock clock) {
      this.clock = clock;
    }
    
    @Override
    public void sleep(long millis) throws InterruptedException {
       clock.increaseCurrentTimeMillis(millis);
    }
    
    public static MockSleeper of(MutableClock clock) {
      return new MockSleeper(clock);
    }
  }

}
