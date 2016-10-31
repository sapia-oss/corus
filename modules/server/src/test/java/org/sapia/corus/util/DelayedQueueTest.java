package org.sapia.corus.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.SysClock.MutableClock;
import org.sapia.corus.util.Sleeper.MockSleeper;
import org.sapia.ubik.util.TimeValue;

public class DelayedQueueTest {
  
  private MutableClock clock;
  private DelayedQueue<String> queue;
  private TimeValue inactivityDelay;
  private TimeValue checkIntervalTime;
  
  @Before
  public void setUp() throws Exception {
    clock             = new MutableClock();
    inactivityDelay   = TimeValue.createMillis(1000);
    checkIntervalTime = TimeValue.createMillis(200);
    
    queue = new DelayedQueue<>(clock, MockSleeper.of(clock), inactivityDelay, checkIntervalTime);
  }

  @Test
  public void testRemoveAllAfterInactivity() throws InterruptedException {
    clock.increaseCurrentTimeMillis(100);
    queue.add("1", "2");
    
    queue.removeAllAfterInactivity(1000);
    
    assertEquals(1100L, clock.currentTimeMillis());
    assertEquals(0, queue.size());
  }
  
  @Test
  public void testRemoveAllAfterInactivity_with_real_clock() throws InterruptedException {
    setUpWithRealClock();
    queue.add("1", "2");
    
    queue.removeAllAfterInactivity(500);
    
    assertEquals(0, queue.size());
  }
  
  @Test
  public void testRemoveAllAfterInactivity_time_out_reached() throws InterruptedException {
    clock.increaseCurrentTimeMillis(100);
    
    List<String> values = queue.removeAllAfterInactivity(1000);
    
    assertEquals(1100L, clock.currentTimeMillis());
    assertEquals(0, values.size());
  }
  
  @Test
  public void testRemoveFirstAfterInactivity() throws InterruptedException {
    clock.increaseCurrentTimeMillis(100);
    queue.add("1", "2");
    
    String value = queue.removeFirstAfterInactivity(1000);
    
    assertEquals(1100L, clock.currentTimeMillis());
    assertEquals(1, queue.size());
    assertEquals("1", value);
  }
  
  @Test(expected = IllegalStateException.class)
  public void testRemoveFirstAfterInactivity_time_out_reached() throws InterruptedException {
    clock.increaseCurrentTimeMillis(100);
    
    queue.removeFirstAfterInactivity(1000);
  }

  @Test
  public void testRemoveLastAfterInactivity() throws InterruptedException {
    clock.increaseCurrentTimeMillis(100);
    queue.add("1", "2");
    
    String value = queue.removeLastAfterInactivity(1000);
    
    assertEquals(1100L, clock.currentTimeMillis());
    assertEquals(1, queue.size());
    assertEquals("2", value);
  }
  
  @Test(expected = IllegalStateException.class)
  public void testRemoveLastAfterInactivity_time_out_reached() throws InterruptedException {
    clock.increaseCurrentTimeMillis(100);
    
    queue.removeLastAfterInactivity(1000);
  }
  
  private void setUpWithRealClock() {
    queue = new DelayedQueue<>(inactivityDelay, checkIntervalTime);
  }
}
