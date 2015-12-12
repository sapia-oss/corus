package org.sapia.corus.client.services.processor;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.Matcheable.Pattern;
import org.sapia.corus.client.common.json.JsonObjectInput;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.exceptions.processor.ProcessLockException;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;

public class ProcessTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test(expected = ProcessLockException.class)
  public void testAcquireLock() throws Exception {
    Process proc = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));
    LockOwner lockOwner = LockOwner.createInstance();
    proc.getLock().acquire(lockOwner);
    LockOwner lockOwner2 = LockOwner.createInstance();
    proc.getLock().acquire(lockOwner2);
  }

  @Test
  public void testIsLocked() throws Exception {
    Process proc = new Process(new DistributionInfo("dist", "1.0", "prod", "app"));
    proc.getLock().acquire(LockOwner.createInstance());
    assertTrue("Process should be locked", proc.getLock().isLocked());
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

    assertEquals(proc1, procs.get(0));
    assertEquals(proc4, procs.get(1));
    assertEquals(proc3, procs.get(2));
    assertEquals(proc2, procs.get(3));
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
    p.toJson(stream, ContentLevel.DETAIL);

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
  public void testFromJson_noNativeProcessOption() throws Exception {
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
    p.toJson(stream, ContentLevel.DETAIL);

    Process copy = Process.fromJson(JsonObjectInput.newInstance(writer.toString()));

    assertEquals(p.isDeleteOnKill(), copy.isDeleteOnKill());
    assertEquals(p.getMaxKillRetry(), copy.getMaxKillRetry());
    assertEquals(p.getProcessID(), copy.getProcessID());
    assertEquals(p.getOsPid(), copy.getOsPid());
    assertEquals(p.getProcessDir(), copy.getProcessDir());
    assertEquals(p.getDistributionInfo().getProcessName(), copy.getDistributionInfo().getProcessName());
    assertEquals(p.getDistributionInfo().getName(), copy.getDistributionInfo().getName());
    assertEquals(p.getDistributionInfo().getVersion(), copy.getDistributionInfo().getVersion());
    assertEquals(p.getDistributionInfo().getProfile(), copy.getDistributionInfo().getProfile());
    assertEquals(p.getShutdownTimeout(), copy.getShutdownTimeout());
    assertEquals(p.getStatus(), copy.getStatus());
    assertEquals(p.getStaleDetectionCount(), copy.getStaleDetectionCount());

    assertEquals(p.getActivePorts().size(), copy.getActivePorts().size());
    for (int i = 0; i < p.getActivePorts().size(); i++) {
      assertEquals(p.getActivePorts().get(i).getName(), copy.getActivePorts().get(i).getName());
      assertEquals(p.getActivePorts().get(i).getPort(), copy.getActivePorts().get(i).getPort());
    }
  }

  @Test
  public void testFromJson_withNativeProcessOption() throws Exception {
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
    p.setNativeProcessOption("test.option1", "0");
    p.setNativeProcessOption("test.option2", "true");
    p.toJson(stream, ContentLevel.DETAIL);

    Process copy = Process.fromJson(JsonObjectInput.newInstance(writer.toString()));

    assertEquals(p.isDeleteOnKill(), copy.isDeleteOnKill());
    assertEquals(p.getMaxKillRetry(), copy.getMaxKillRetry());
    assertEquals(p.getProcessID(), copy.getProcessID());
    assertEquals(p.getOsPid(), copy.getOsPid());
    assertEquals(p.getProcessDir(), copy.getProcessDir());
    assertEquals(p.getDistributionInfo().getProcessName(), copy.getDistributionInfo().getProcessName());
    assertEquals(p.getDistributionInfo().getName(), copy.getDistributionInfo().getName());
    assertEquals(p.getDistributionInfo().getVersion(), copy.getDistributionInfo().getVersion());
    assertEquals(p.getDistributionInfo().getProfile(), copy.getDistributionInfo().getProfile());
    assertEquals(p.getShutdownTimeout(), copy.getShutdownTimeout());
    assertEquals(p.getStatus(), copy.getStatus());
    assertEquals(p.getStaleDetectionCount(), copy.getStaleDetectionCount());

    assertEquals(p.getActivePorts().size(), copy.getActivePorts().size());
    for (int i = 0; i < p.getActivePorts().size(); i++) {
      assertEquals(p.getActivePorts().get(i).getName(), copy.getActivePorts().get(i).getName());
      assertEquals(p.getActivePorts().get(i).getPort(), copy.getActivePorts().get(i).getPort());
    }

    Assertions.assertThat(copy.getNativeProcessOptions()).containsOnly(
        MapEntry.entry("test.option1", "0"), MapEntry.entry("test.option2", "true"));
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

  @Test
  public void testMatchesProcessCriteria_exact_dist_name_and_version() {
    Process proc1 = new Process(new DistributionInfo("dist1", "1.0", "prod", "app"));
    Process proc2 = new Process(new DistributionInfo("dist2", "2.0", "prod", "app"));

    ProcessCriteria c = ProcessCriteria.builder().distribution("dist1").version("1.0").build();

    assertTrue(proc1.matches(c));
    assertFalse(proc2.matches(c));
  }

  @Test
  public void testMatchesProcessCriteria_any_dist_name_version() {
    Process proc1 = new Process(new DistributionInfo("dist1", "1.0", "prod", "app"));
    Process proc2 = new Process(new DistributionInfo("dist2", "2.0", "prod", "app"));

    ProcessCriteria c = ProcessCriteria.builder().all();

    assertTrue(proc1.matches(c));
    assertTrue(proc2.matches(c));
  }

  @Test
  public void testMatchesProcessCriteria_profile() {
    Process proc1 = new Process(new DistributionInfo("dist1", "1.0", "prod", "app"));
    Process proc2 = new Process(new DistributionInfo("dist2", "2.0", "dev", "app"));

    ProcessCriteria c = ProcessCriteria.builder().profile("dev").build();

    assertFalse(proc1.matches(c));
    assertTrue(proc2.matches(c));
  }

  @Test
  public void testMatchesProcessCriteria_process() {
    Process proc1 = new Process(new DistributionInfo("dist1", "1.0", "prod", "app1"));
    Process proc2 = new Process(new DistributionInfo("dist2", "2.0", "dev", "app2"));

    ProcessCriteria c = ProcessCriteria.builder().name(ArgMatchers.exact("app2")).build();

    assertFalse(proc1.matches(c));
    assertTrue(proc2.matches(c));
  }

  @Test
  public void testMatchesProcessCriteria_status() {
    Process proc1 = new Process(new DistributionInfo("dist1", "1.0", "prod", "app1"));
    proc1.setStatus(LifeCycleStatus.ACTIVE);
    Process proc2 = new Process(new DistributionInfo("dist2", "2.0", "dev", "app2"));
    proc2.setStatus(LifeCycleStatus.STALE);

    ProcessCriteria c = ProcessCriteria.builder().lifecycles(LifeCycleStatus.STALE).build();

    assertFalse(proc1.matches(c));
    assertTrue(proc2.matches(c));
  }

  @Test
  public void testMatchesProcessCriteria_port() {
    Process proc1 = new Process(new DistributionInfo("dist1", "1.0", "prod", "app1"));
    Process proc2 = new Process(new DistributionInfo("dist2", "2.0", "dev", "app2"));
    proc2.addActivePort(new ActivePort("test", 8080));

    ProcessCriteria c = ProcessCriteria.builder().ports(PortCriteria.builder().range("test").port(8080).build()).build();

    assertFalse(proc1.matches(c));
    assertTrue(proc2.matches(c));
  }

  @Test
  public void testMatchesProcessCriteria_port_multiple_processes() {
    Process proc1 = new Process(new DistributionInfo("dist1", "1.0", "prod", "app1"));
    proc1.addActivePort(new ActivePort("test1", 8080));

    Process proc2 = new Process(new DistributionInfo("dist2", "2.0", "dev", "app2"));
    proc2.addActivePort(new ActivePort("test2", 8080));

    ProcessCriteria c = ProcessCriteria.builder().ports(PortCriteria.builder().range("test2").port(8080).build()).build();

    assertFalse(proc1.matches(c));
    assertTrue(proc2.matches(c));
  }

  @Test
  public void testMatchesProcessCriteria_any_port_multiple_processes() {
    Process proc1 = new Process(new DistributionInfo("dist1", "1.0", "prod", "app1"));
    proc1.addActivePort(new ActivePort("test", 8100));

    Process proc2 = new Process(new DistributionInfo("dist2", "2.0", "dev", "app2"));
    proc2.addActivePort(new ActivePort("test", 8101));

    ProcessCriteria c = ProcessCriteria.builder().ports(PortCriteria.builder().range("test").port(ArgMatchers.any()).build()).build();

    assertTrue(proc1.matches(c));
    assertTrue(proc2.matches(c));
  }
}
