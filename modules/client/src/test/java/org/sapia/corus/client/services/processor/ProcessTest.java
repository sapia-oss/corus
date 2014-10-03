package org.sapia.corus.client.services.processor;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.Matcheable.Pattern;
import org.sapia.corus.client.exceptions.processor.ProcessLockException;

public class ProcessTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testAcquireLock() throws Exception {
    Process proc = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));
    LockOwner lockOwner = LockOwner.createInstance();
    proc.getLock().acquire(lockOwner);
    LockOwner lockOwner2 = LockOwner.createInstance();
    try {
      proc.getLock().acquire(lockOwner2);
      Assert.fail("Process lock should not have been acquired");
    } catch (ProcessLockException e) {
      // ok
    }
  }

  @Test
  public void testIsLocked() throws Exception {
    Process proc = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));
    proc.getLock().acquire(LockOwner.createInstance());
    Assert.assertTrue("Process should be locked", proc.getLock().isLocked());
  }

  @Test
  public void testSort() throws Exception {
    Process proc1 = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));
    Process proc2 = new Process(new DistributionInfo("dist", "2.0", "prod", "app"));
    Process proc3 = new Process(new DistributionInfo("dist", "1.0", "prod", "app2"));
    Process proc4 = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));

    List<Process> procs = new ArrayList<Process>();
    procs.add(proc1);
    procs.add(proc2);
    procs.add(proc3);
    procs.add(proc4);

    Collections.sort(procs);

    Assert.assertEquals(proc1, procs.get(0));
    Assert.assertEquals(proc4, procs.get(1));
    Assert.assertEquals(proc3, procs.get(2));
    Assert.assertEquals(proc2, procs.get(3));

  }
  
  @Test
  public void testMatchesDist() {
    Process proc = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));
    Pattern pattern = Matcheable.DefaultPattern.parse("dis*");
    assertTrue(proc.matches(pattern));
  }

  @Test
  public void testMatchesVersion() {
    Process proc = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));
    Pattern pattern = Matcheable.DefaultPattern.parse("1.*");
    assertTrue(proc.matches(pattern));
  }
  
  @Test
  public void testMatchesProfile() {
    Process proc = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));
    Pattern pattern = Matcheable.DefaultPattern.parse("pro*");
    assertTrue(proc.matches(pattern));
  }
  
  @Test
  public void testMatchesName() {
    Process proc = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));
    Pattern pattern = Matcheable.DefaultPattern.parse("ap*");
    assertTrue(proc.matches(pattern));
  }
  
  @Test
  public void testMatchesOsPid() {
    Process proc = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));
    proc.setOsPid("1234");
    Pattern pattern = Matcheable.DefaultPattern.parse("123*");
    assertTrue(proc.matches(pattern));
  }
  
  @Test
  public void testMatchesPid() {
    Process proc = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));
    Pattern pattern = Matcheable.DefaultPattern.parse(proc.getProcessID());
    assertTrue(proc.matches(pattern));
  }
}
