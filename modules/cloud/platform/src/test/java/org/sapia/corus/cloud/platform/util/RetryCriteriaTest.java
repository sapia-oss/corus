package org.sapia.corus.cloud.platform.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.cloud.platform.util.RetryCriteria;
import org.sapia.corus.cloud.platform.util.TimeMeasure;
import org.sapia.corus.cloud.platform.util.TimeSupplier.MutableTime;
import org.sapia.corus.cloud.platform.util.TimeSupplier.SystemTime;

public class RetryCriteriaTest {

  private MutableTime clock;
  
  @Before
  public void setUp() {
    clock = MutableTime.getInstance();
  }
  
  @Test
  public void testPause() throws InterruptedException {
    RetryCriteria crit  = new RetryCriteria(TimeMeasure.forMillis(500), 5);
    TimeMeasure   start = TimeMeasure.forCurrentTime(SystemTime.getInstance());
    crit.pause();
    assertTrue(start.elapsedMillis().getMillis() >= 500);
  }

  @Test
  public void testForMaxDuration_max_time_reached() {
    TimeMeasure start = TimeMeasure.forCurrentTime(clock);
    RetryCriteria crit = RetryCriteria.forMaxDuration(TimeMeasure.forMillis(1000).withTimeSupplier(clock), TimeMeasure.forMillis(5000).withTimeSupplier(clock));
    clock.addTime(5000);
    assertTrue(crit.isOver(0, start));
  }
  
  @Test
  public void testForMaxDuration_max_attempts_reached() {
    TimeMeasure start = TimeMeasure.forCurrentTime(clock);
    RetryCriteria crit = RetryCriteria.forMaxDuration(TimeMeasure.forMillis(1000).withTimeSupplier(clock), TimeMeasure.forMillis(5000).withTimeSupplier(clock));
    assertTrue(crit.isOver(5, start));
  }
  
  @Test
  public void testForMaxDuration_false() {
    TimeMeasure start = TimeMeasure.forCurrentTime(clock);
    RetryCriteria crit = RetryCriteria.forMaxDuration(TimeMeasure.forMillis(1000).withTimeSupplier(clock), TimeMeasure.forMillis(5000).withTimeSupplier(clock));
    assertFalse(crit.isOver(4, start));
  }
  
  @Test
  public void testForMaxDuration_check_max_number_of_attempts() {
    RetryCriteria crit = RetryCriteria.forMaxDuration(TimeMeasure.forMillis(1000).withTimeSupplier(clock), TimeMeasure.forMillis(5000).withTimeSupplier(clock));
    assertEquals(5, crit.getMaxAttempts());
  }
  
  @Test
  public void testForMaxAttempts() {
    RetryCriteria crit = RetryCriteria.forMaxAttempts(TimeMeasure.forMillis(1000).withTimeSupplier(clock), 5);
    assertTrue(crit.isOver(5, TimeMeasure.forCurrentTime(clock)));
  }
  
  @Test
  public void testForMaxAttempts_false() {
    RetryCriteria crit = RetryCriteria.forMaxAttempts(TimeMeasure.forMillis(1000).withTimeSupplier(clock), 5);
    assertFalse(crit.isOver(4, TimeMeasure.forCurrentTime(clock)));
  }

}
