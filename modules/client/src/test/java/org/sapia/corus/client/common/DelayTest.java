package org.sapia.corus.client.common;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.Delay;

public class DelayTest {

  Delay delay;
  
  @Before
  public void setUp() throws Exception {
    delay = new Delay(2, TimeUnit.SECONDS);
  }
  
  @Test
  public void testIsOver() throws Exception{
    assertFalse("Delay should not be over", delay.start().isOver());
    assertTrue("Should have time remaining", delay.remainingMillis() > 0);
    Thread.sleep(2000);
    assertTrue("Delay should be over", delay.isOver());
    assertTrue("Should not have time remaining", delay.remainingMillis() == 0);

  }

}
