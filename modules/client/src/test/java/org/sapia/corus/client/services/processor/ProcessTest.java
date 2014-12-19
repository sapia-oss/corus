package org.sapia.corus.client.services.processor;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.Matcheable.Pattern;
import org.sapia.corus.client.exceptions.processor.ProcessLockException;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;

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
  public void testToJson() throws Exception {
    StringWriter     writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
    
    Process p = new Process(new DistributionInfo("test-dist", "1.0", "test-profile", "test-process"), "test-id");
    p.setDeleteOnKill(true);
    p.setMaxKillRetry(5);
    p.setOsPid("test-os-pid");
    p.setProcessDir("test-dir");
    p.setShutdownTimeout(5);
    p.setStatus(LifeCycleStatus.KILL_CONFIRMED);
    p.addActivePort(new ActivePort("port0", 0));
    p.addActivePort(new ActivePort("port1", 1));
    p.incrementStaleDetectionCount();
    p.toJson(stream);
    
    JSONObject json = JSONObject.fromObject(writer.toString());
    
    assertEquals(true, json.getBoolean("deleteOnKill"));
    assertEquals(5, json.getInt("maxKillRetry"));
    assertEquals("test-os-pid", json.getString("pid"));
    assertEquals("test-process", json.getString("name"));
    assertEquals("test-dist", json.getString("distribution"));
    assertEquals("1.0", json.getString("version"));
    assertEquals("test-profile", json.getString("profile"));
    assertEquals(5, json.getLong("shutdownTimeout"));
    assertEquals(LifeCycleStatus.KILL_CONFIRMED.name(), json.getString("status"));
    assertEquals(1, json.getInt("staleDetectionCount"));
    
    WriterJsonStream.parseDate(json.getString("creationTimestamp"));
    WriterJsonStream.parseDate(json.getString("lastAccessTimestamp"));
    
    JSONArray activePorts = json.getJSONArray("activePorts");
    for (int i = 0; i < activePorts.size(); i++) {
      JSONObject ap = activePorts.getJSONObject(i);
      assertEquals(i, ap.getInt("port"));
      assertEquals("port" + i, ap.getString("name"));
    }
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
