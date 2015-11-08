package org.sapia.corus.cloud.platform.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sapia.corus.cloud.platform.util.RetryCriteria;
import org.sapia.corus.cloud.platform.util.RetryLatch;
import org.sapia.corus.cloud.platform.util.TimeMeasure;
import org.sapia.corus.cloud.platform.util.TimeSupplier;
import org.sapia.corus.cloud.platform.util.RetryLatch.RetryListener;

public class RetryLatchTest {
  
  private RetryLatch  latch;
  
  @Before
  public void setUp() {
    latch = new RetryLatch(RetryCriteria.forMaxAttempts(TimeMeasure.forMillis(200), 5));
  }
 
  @Test
  public void testIncrement_should_continue() {
    assertTrue(latch.increment().shouldContinue());
  }

  @Test
  public void testIncrement_should_stop() {
    for (int i = 0; i < 4; i++) {
      latch.increment();
    }
    assertTrue(latch.increment().shouldStop());
  }
  
  @Test
  public void testShouldStop_false() {
    assertFalse(latch.shouldStop());
  }

  @Test
  public void testShouldStop_true() {
    for (int i = 0; i < 4; i++) {
      latch.increment();
    }
    assertFalse(latch.shouldStop());
  }
  
  @Test
  public void testShouldContinue_true() {
    assertTrue(latch.shouldContinue());
  }

  @Test
  public void testShouldContinue_false() {
    for (int i = 0; i < 5; i++) {
      latch.increment();
    }
    assertFalse(latch.shouldContinue());
  }

  @Test
  public void testIncrementAndPause_no_pause() throws InterruptedException {
    TimeMeasure start = TimeMeasure.forCurrentTime(TimeSupplier.SystemTime.getInstance());
    for (int i = 0; i < 5; i++) {
      latch.increment();
    }
    latch.incrementAndPause();
    assertTrue(start.elapsedMillis().getValue() < 200);
  }
  
  @Test
  public void testIncrementAndPause_pause_observed() throws InterruptedException {
    for (int i = 0; i < 3; i++) {
      latch.increment();
    }
    TimeMeasure start = TimeMeasure.forCurrentTime(TimeSupplier.SystemTime.getInstance());
    latch.incrementAndPause();
    assertTrue(start.elapsedMillis().getValue() >= 200);
  }
  
  @Test
  public void testPause() throws InterruptedException {
    TimeMeasure start = TimeMeasure.forCurrentTime(TimeSupplier.SystemTime.getInstance());
    for (int i = 0; i < 4; i++) {
      latch.increment();
    }
    latch.pause();
    assertTrue(start.elapsedMillis().getValue() >= 200);
  }

  @Test
  public void testWithRetryListener() {
    RetryListener mockListener = Mockito.mock(RetryListener.class);
    latch.withRetryListener(mockListener);
    latch.increment();
    latch.increment();
    verify(mockListener).onRetry(1);
    verify(mockListener).onRetry(2);
  }

}
