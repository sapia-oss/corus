package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;


/**
 * @author Yanick Duchesne
 */
public class ProcessorStoreTest extends TestCase {
  
  ProcessRepository        db;
  
  List<Process> procs = new ArrayList<Process>();
  
  /**
   * Constructor for VmProcessorTest.
   * @param arg0
   */
  public ProcessorStoreTest(String arg0) {
    super(arg0);
  }
  
  protected void setUp() throws Exception {
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
    
    db.getActiveProcesses().addProcess(proc1);
    db.getActiveProcesses().addProcess(proc2);
    db.getActiveProcesses().addProcess(proc3);
    db.getActiveProcesses().addProcess(proc4);    
  }
  
  public void testGetProcessByID() throws Exception{
    db.getActiveProcesses().getProcess(((Process)procs.get(0)).getProcessID());
    db.getActiveProcesses().getProcess(((Process)procs.get(1)).getProcessID());
    db.getActiveProcesses().getProcess(((Process)procs.get(2)).getProcessID());
    db.getActiveProcesses().getProcess(((Process)procs.get(3)).getProcessID());
  }

  public void testGetAllProcesses() throws Exception {
    super.assertEquals(4, db.getActiveProcesses().getProcesses(ProcessCriteria.builder().all()).size());
    
  }
  
  public void testGetProcessesForName() throws Exception{
    super.assertEquals(3, db.getActiveProcesses().getProcesses(ProcessCriteria.builder().distribution("test").build()).size());
  }
  
  public void testGetProcessesForNameVersion() throws Exception{
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("test").version("1.0").build();
    super.assertEquals(2, db.getActiveProcesses().getProcesses(criteria).size());
  }
  
  public void testGetProcessesForNameVersionProfile() throws Exception{
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("test").version("1.0").profile("test").build();
    super.assertEquals(2,
                       db.getActiveProcesses()
                         .getProcesses(criteria).size());
    
  }
  
  public void testGetProcessesForNameVersionProfileProcess() throws Exception{
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("test").version("1.0").profile("test").name("testVm").build();
    super.assertEquals(2,
                       db.getActiveProcesses()
                         .getProcesses(criteria).size());
    
    criteria = ProcessCriteria.builder().distribution("test").version("1.1").build();    
    super.assertEquals(1,
                       db.getActiveProcesses().getProcesses(criteria).size());
  }
  
  public void testGetProcessDistNamePattern() throws Exception{
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("*").build();    
    super.assertEquals(4, db.getActiveProcesses().getProcesses(criteria).size());
    criteria = ProcessCriteria.builder().distribution("test*").build();
    super.assertEquals(3, db.getActiveProcesses().getProcesses(criteria).size());    
  }
  
  public void testGetProcessDistNameVersionPattern() throws Exception{
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("*").version("*.0").build();
    super.assertEquals(2, db.getActiveProcesses().getProcesses(criteria).size());
    criteria = ProcessCriteria.builder().distribution("*").version("*").build();
    super.assertEquals(4, db.getActiveProcesses().getProcesses(criteria).size());
    criteria = ProcessCriteria.builder().distribution("*").version("1.1").build();
    super.assertEquals(2, db.getActiveProcesses().getProcesses(criteria).size());
  }
  
  public void testGetProcessDistNameVersionAndNamePattern() throws Exception{
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("*").version("*").name("other*").build();    
    super.assertEquals(1, db.getActiveProcesses().getProcesses(criteria).size());    
    
    criteria = ProcessCriteria.builder().distribution("*").version("*").profile("someTest").name("other*").build();
    super.assertEquals(0, db.getActiveProcesses().getProcesses(criteria).size());    
    
    criteria = ProcessCriteria.builder().distribution("*").version("*").profile("test").name("other*").build();
    super.assertEquals(1, db.getActiveProcesses().getProcesses(criteria).size());    
  }    

  public void testRemoveProcess() throws Exception {
    ProcessRepository        db   = new TestProcessRepository();
    DistributionInfo dist = new DistributionInfo("test", "1.0", "test", "testVm");
    Process          proc = new Process(dist);
    db.getActiveProcesses().addProcess(proc);
    db.getActiveProcesses().removeProcess(proc.getProcessID());

    try {
      db.getActiveProcesses().getProcess(proc.getProcessID());
      throw new Exception("process not removed");
    } catch (ProcessNotFoundException e) {
      // ok
    }
  }

  public void testRemoveMultiProcess() throws Exception {
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("test").version("1.0").build();    
    db.getActiveProcesses().removeProcesses(criteria);
    
    super.assertEquals(0,
                       db.getActiveProcesses().getProcesses(criteria).size());
  }
  
  public void testRemoveMultiProcessDistNamePattern() throws Exception {
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("test*").version("1.0").build();    
    db.getActiveProcesses().removeProcesses(criteria);
    
    super.assertEquals(0,
                       db.getActiveProcesses().getProcesses(criteria).size());
  }  

  public void testRemoveMultiProcessDistNameVersionPattern() throws Exception {
    ProcessCriteria criteria = ProcessCriteria.builder().distribution("test*").version("1.*").build();    
    db.getActiveProcesses().removeProcesses(criteria);
    
    super.assertEquals(1,
                       db.getActiveProcesses().getProcesses(ProcessCriteria.builder().all()).size());
  }  
  
}
