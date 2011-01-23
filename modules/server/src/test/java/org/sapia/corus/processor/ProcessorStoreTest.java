package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;


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

  public void testGetProcessExactNameAndVersion() throws Exception {
    
    super.assertEquals(4, db.getActiveProcesses().getProcesses().size());
    super.assertEquals(3, db.getActiveProcesses().getProcesses(ArgFactory.parse("test")).size());
    super.assertEquals(2,
                       db.getActiveProcesses().getProcesses(ArgFactory.parse("test"), ArgFactory.parse("1.0")).size());
    super.assertEquals(2,
                       db.getActiveProcesses()
                         .getProcesses(ArgFactory.parse("test"), ArgFactory.parse("1.0"), "test").size());
    super.assertEquals(2,
                       db.getActiveProcesses()
                         .getProcesses(ArgFactory.parse("test"), ArgFactory.parse("1.0"), 
                             "test", ArgFactory.parse("testVm")).size());
    super.assertEquals(1,
                       db.getActiveProcesses().getProcesses(ArgFactory.parse("test"), ArgFactory.parse("1.1")).size());
  }
  
  public void testGetProcessDistNamePattern() throws Exception{
    super.assertEquals(4, db.getActiveProcesses().getProcesses(ArgFactory.parse("*")).size());
    super.assertEquals(3, db.getActiveProcesses().getProcesses(ArgFactory.parse("test*")).size());    
  }
  
  public void testGetProcessDistNameVersionPattern() throws Exception{
    super.assertEquals(2, db.getActiveProcesses().getProcesses(ArgFactory.parse("*"), ArgFactory.parse("*.0")).size());
    super.assertEquals(4, db.getActiveProcesses().getProcesses(ArgFactory.parse("*"), ArgFactory.parse("*")).size());    
    super.assertEquals(2, db.getActiveProcesses().getProcesses(ArgFactory.parse("*"), ArgFactory.parse("1.1")).size());
  }
  
  public void testGetProcessDistNameVersionAndNamePattern() throws Exception{
    super.assertEquals(1, db.getActiveProcesses().getProcesses(
        ArgFactory.parse("*"), 
        ArgFactory.parse("*"), null, 
        ArgFactory.parse("other*")).size());    
    
    super.assertEquals(0, db.getActiveProcesses().getProcesses(
        ArgFactory.parse("*"), 
        ArgFactory.parse("*"), "someTest", 
        ArgFactory.parse("other*")).size());    
    
    super.assertEquals(1, db.getActiveProcesses().getProcesses(
        ArgFactory.parse("*"), 
        ArgFactory.parse("*"), "test", 
        ArgFactory.parse("other*")).size());    
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
    db.getActiveProcesses().removeProcesses(ArgFactory.parse("test"), ArgFactory.parse("1.0"));
    
    super.assertEquals(0,
                       db.getActiveProcesses().getProcesses(ArgFactory.parse("test"), ArgFactory.parse("1.0")).size());
  }
  
  public void testRemoveMultiProcessDistNamePattern() throws Exception {
    db.getActiveProcesses().removeProcesses(ArgFactory.parse("test*"), ArgFactory.parse("1.0"));
    
    super.assertEquals(0,
                       db.getActiveProcesses().getProcesses(ArgFactory.parse("test"), ArgFactory.parse("1.0")).size());
  }  

  public void testRemoveMultiProcessDistNameVersionPattern() throws Exception {
    db.getActiveProcesses().removeProcesses(ArgFactory.parse("test*"), ArgFactory.parse("1.*"));
    
    super.assertEquals(1,
                       db.getActiveProcesses().getProcesses(ArgFactory.parse("*"), ArgFactory.parse("*")).size());
  }  
  
}
