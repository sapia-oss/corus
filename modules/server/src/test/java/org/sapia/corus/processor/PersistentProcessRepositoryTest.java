package org.sapia.corus.processor;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.database.CachingDbMap;
import org.sapia.corus.database.TestDbModule;
import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.Param;
import org.sapia.corus.interop.Status;

public class PersistentProcessRepositoryTest {

  private static TestDbModule db;
  private static ProcessRepository repo;
  
  @BeforeClass
  public static void setUp() throws Exception {
    db = new TestDbModule();
    db.setup();

    DbMap<String, org.sapia.corus.client.services.processor.Process> processDb = db.createDbMap(
        "processes", 
        String.class, org.sapia.corus.client.services.processor.Process.class
    );
    processDb = new CachingDbMap<String, Process>(processDb);
    repo = new ProcessRepositoryImpl(new ProcessDatabaseImpl(processDb));
  }
  
  @AfterClass
  public static void tearDown(){
    db.teardown();
  }
  
  @Test
  public void testAddProcess() throws Exception {
    DistributionInfo info = new DistributionInfo("test", "1.0", "prod", "app");
    Process proc = new Process(info);
    repo.addProcess(proc);
    repo.getProcess(proc.getProcessID());
  }
  
  @Test(expected = ProcessNotFoundException.class)
  public void testRemoveProcess() throws Exception{
    DistributionInfo info = new DistributionInfo("test", "1.0", "prod", "app");
    Process proc = new Process(info);
    
    repo.addProcess(proc);
    repo.removeProcess(proc.getProcessID());
    repo.getProcess(proc.getProcessID());
  }
  
  @Test
  public void testUpdateProcess() throws Exception{
    DistributionInfo info = new DistributionInfo("test", "1.0", "prod", "app");
    Process proc = new Process(info);
    
    repo.addProcess(proc);
    proc.setOsPid("1234");
    proc.save();
    
    Process updated = repo.getProcess(proc.getProcessID());
    assertEquals("1234", updated.getOsPid());
  }
  
  @Test
  public void testLock() throws Exception{
    DistributionInfo info = new DistributionInfo("test", "1.0", "prod", "app");
    Process proc = new Process(info);
    
    LockOwner lck = new LockOwner();
    repo.addProcess(proc);
    proc.getLock().acquire(lck);
    proc.save();
    
    Process updated = repo.getProcess(proc.getProcessID());
    assertTrue("Process should be locked", updated.getLock().isLocked());
  }

  @Test
  public void testStatus() throws Exception{
    DistributionInfo info = new DistributionInfo("test", "1.0", "prod", "app");
    Process proc = new Process(info);
    
    repo.addProcess(proc);
    
    Status stat = new Status();
    Context ctx = new Context("aContext");
    ctx.addParam(new Param("aName", "aValue"));
    proc.status(stat);
    proc.save();
    
    Process withStatus = repo.getProcess(proc.getProcessID());
    assertTrue(withStatus.getProcessStatus() != null);
    
    boolean found = false;
    for(Process p: repo.getProcesses(ProcessCriteria.builder().all())){
      if(p.getProcessID().equals(proc.getProcessID())){
        assertTrue(p.getProcessStatus() != null);
        found = true;
      }
    }
    
    if(!found){
      fail(String.format("Process not found: %s", proc.getProcessID()));
    }

  }

  
}
