package org.sapia.corus.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class TimeUtilTest {

  @Test
  public void testCreateRandomDelay() {
    long value = TimeUtil.createRandomDelay(1000, 15);
    assertTrue(value > 0);
  }

}
