package org.sapia.corus.processor;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;

public class ProcessorRepositoryTest {
  
  ProcessRepository db;
  
  List<Process> procs = new ArrayList<Process>();
  
  @Before
  public void setUp() throws Exception {
    db = new TestProcessRepository();
    DistributionInfo dist1 = new DistributionInfo("test", "1.0", "test",
    "testVm");
    DistributionInfo dist2 = new DistributionInfo("test", "1.1", "test",
        "testVm");
    
    DistributionInfo dist3 = new DistributionInfo("otherTest", "1.1", "test",
    "otherTestVm");
    
    Process          proc1 = new Process(dist1);
    Process          proc2 = new Process(dist1);
    Process          proc3 = new Process(dist2);
    Process          proc4 = new Process(dist3);
    
    procs.add(proc1);
    procs.add(proc2);
    procs.add(proc3);
    procs.add(proc4);
    
    db.addProcess(proc1);
    db.addProcess(proc2);
    db.addProcess(proc3);
    db.addProcess(proc4);    
  }
  
  @Test
  public void testGetProcessByID() throws Exception{
    db.getProcess(procs.get(0).getProcessID());
    db.getProcess(procs.get(1).getProcessID());
    db.getProcess(procs.get(2).getProcessID());
    db.getProcess(procs.get(3).getProcessID());
  }

  @Test
  public void testGetAllProcesses() throws Exception {
    assertEquals(4, db.getProcesses(ProcessCriteria.builder().all()).size());
    
  }
  
  @Test
  public void testGetProcessesForName() throws Exception{
    assertEquals(3, db.getProcesses(ProcessCriteria.builder().distribution("test").build()).size());
  }
  
  @Test
  public void testGetProcessesForNameVersion() throws Exception{
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("test").version("1.0").build();
    assertEquals(2, db.getProcesses(criteria).size());
  }
  
  @Test
  public void testGetProcessesForNameVersionProfile() throws Exception{
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("test").version("1.0").profile("test").build();
    assertEquals(2, db.getProcesses(criteria).size());
    
  }
  
  @Test
  public void testGetProcessesForNameVersionProfileProcess() throws Exception{
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("test").version("1.0").profile("test").name("testVm").build();
    assertEquals(2, db.getProcesses(criteria).size());
    
    criteria = ProcessCriteria.builder().distribution("test").version("1.1").build();    
    assertEquals(1, db.getProcesses(criteria).size());
  }
  
  @Test
  public void testGetProcessDistNamePattern() throws Exception{
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("*").build();    
    assertEquals(4, db.getProcesses(criteria).size());
    criteria = ProcessCriteria.builder().distribution("test*").build();
    assertEquals(3, db.getProcesses(criteria).size());    
  }
  
  @Test
  public void testGetProcessDistNameVersionPattern() throws Exception{
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("*").version("*.0").build();
    assertEquals(2, db.getProcesses(criteria).size());
    criteria = ProcessCriteria.builder().distribution("*").version("*").build();
    assertEquals(4, db.getProcesses(criteria).size());
    criteria = ProcessCriteria.builder().distribution("*").version("1.1").build();
    assertEquals(2, db.getProcesses(criteria).size());
  }
  
  @Test
  public void testGetProcessDistNameVersionAndNamePattern() throws Exception{
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("*").version("*").name("other*").build();    
    assertEquals(1, db.getProcesses(criteria).size());    
    
    criteria = ProcessCriteria.builder().distribution("*").version("*").profile("someTest").name("other*").build();
    assertEquals(0, db.getProcesses(criteria).size());    
    
    criteria = ProcessCriteria.builder().distribution("*").version("*").profile("test").name("other*").build();
    assertEquals(1, db.getProcesses(criteria).size());    
  }    

  @Test
  public void testRemoveProcess() throws Exception {
    ProcessRepository        db   = new TestProcessRepository();
    DistributionInfo dist = new DistributionInfo("test", "1.0", "test", "testVm");
    Process          proc = new Process(dist);
    db.addProcess(proc);
    db.removeProcess(proc.getProcessID());
  }

  
}
