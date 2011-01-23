package org.sapia.corus.processor;

import junit.framework.TestCase;

public class StartupLockTest extends TestCase {

  public StartupLockTest(String arg0) {
    super(arg0);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testAuthorize() throws Exception{
    StartupLock lock = new StartupLock(500);
    assertTrue(lock.authorize());
    assertFalse(lock.authorize());
    Thread.sleep(1000);
    assertTrue(lock.authorize());    
  }

}
