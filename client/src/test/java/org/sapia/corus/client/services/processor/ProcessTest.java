package org.sapia.corus.client.services.processor;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.exceptions.processor.ProcessLockException;

public class ProcessTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testAcquireLock() throws Exception{
    Process proc = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));
    LockOwner lockOwner = Process.createLockOwner();
    proc.acquireLock(lockOwner);
    LockOwner lockOwner2 = Process.createLockOwner();
    try {
      proc.acquireLock(lockOwner2);
      Assert.fail("Process lock should not have been acquired");
    } catch (ProcessLockException e) {
      //ok
    }
  }
  
  @Test
  public void testIsLocked() throws Exception{
    Process proc = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));
    proc.acquireLock(Process.createLockOwner());
    Assert.assertTrue("Process should be locked", proc.isLocked());
  }


}
