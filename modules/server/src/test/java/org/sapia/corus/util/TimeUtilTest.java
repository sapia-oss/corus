package org.sapia.corus.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.sapia.ubik.util.TimeRange;

public class TimeUtilTest {

  @Test
  public void testCreateRandomDelay() {
    long value = TimeUtil.createRandomDelay(1000, 1500);
    assertTrue(value >= 1000 && value < 1500);
  }

  
  @Test
  public void testCreateRandomDelay_with_time_range() {
    long value = TimeUtil.createRandomDelay(TimeRange.valueOf("1000:1500"));
    assertTrue(value >= 1000 && value < 1500);
  }

}
