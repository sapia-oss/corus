package org.sapia.corus.processor.task;

import org.sapia.corus.LogicException;
import org.sapia.corus.port.TestPortManager;
import org.sapia.corus.processor.*;
import org.sapia.corus.processor.Process;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.corus.util.ProgressQueueImpl;

import org.sapia.taskman.TaskContext;
import org.sapia.ubik.net.TCPAddress;


/**
 * @author Yanick Duchesne
 */
public class KillTaskTest extends BaseTaskTest{
  
  /**
   * Constructor for KillTaskTest.
   * @param arg0
   */
  public KillTaskTest(String arg0) {
    super(arg0);
  }
  
  public void testKillFromCorusNotConfirmed() throws Exception {
    ProcessDB        db   = new TestProcessDB();
    DistributionInfo dist = new DistributionInfo("test", "1.0", "test", "testVm");
    Process          proc = new Process(dist);
    db.getActiveProcesses().addProcess(proc);

    TestKill kill = new TestKill(Process.KILL_REQUESTOR_SERVER, db,
                                 proc.getProcessID());
    _tm.execSyncTask("kill", kill);
    _tm.execSyncTask("kill", kill);
    _tm.execSyncTask("kill", kill);
    super.assertTrue(kill.killed != true);
  }

  public void testKillFromCorusConfirmed() throws Exception {
    ProcessDB        db   = new TestProcessDB();
    DistributionInfo dist = new DistributionInfo("test", "1.0", "test", "testVm");
    Process          proc = new Process(dist);
    db.getActiveProcesses().addProcess(proc);

    TestKill kill = new TestKill(Process.KILL_REQUESTOR_SERVER, db,
                                 proc.getProcessID());
    proc.confirmKilled();
      
    _tm.execSyncTask("kill", kill);
    _tm.execSyncTask("kill", kill);    
    _tm.execSyncTask("kill", kill);    
    super.assertTrue(kill.killed);

    try {
      db.getActiveProcesses().getProcess(proc.getProcessID());
      throw new Exception("Process not removed from db");
    } catch (LogicException e) {
      //ok
    }

    super.assertTrue(!kill.restart);
  }

  public void testKillMaxAttemptReached() throws Exception {
    
    ProcessDB        db   = new TestProcessDB();
    DistributionInfo dist = new DistributionInfo("test", "1.0", "test", "testVm");
    Process          proc = new Process(dist);
    db.getActiveProcesses().addProcess(proc);

    Thread.sleep(1500);

    ProgressQueue q = new ProgressQueueImpl();

    TestKill      kill = new TestKill(Process.KILL_REQUESTOR_SERVER, db,
                                      proc.getProcessID());
    _tm.execSyncTask("kill", kill);
    super.assertTrue(!kill.killed);
    super.assertTrue(!kill.restart);
    _tm.execSyncTask("kill", kill);
    super.assertTrue(!kill.killed);
    super.assertTrue(!kill.restart);
    _tm.execSyncTask("kill", kill);
    proc.confirmKilled();    
    _tm.execSyncTask("kill", kill);    
    super.assertTrue(kill.killed);

    try {
      db.getActiveProcesses().getProcess(proc.getProcessID());
      throw new Exception("Process not removed from db");
    } catch (LogicException e) {
      //ok
    }
  }

  public void testRestart() throws Exception {
    ProcessDB        db   = new TestProcessDB();
    DistributionInfo dist = new DistributionInfo("test", "1.0", "test", "testVm");
    Process          proc = new Process(dist);
    db.getActiveProcesses().addProcess(proc);
    Thread.sleep(1500);

    TestKill kill = new TestKill(Process.KILL_REQUESTOR_SERVER, db,
                                 proc.getProcessID(), 500L);
    proc.confirmKilled();
    _tm.execSyncTask("kill", kill);
    super.assertTrue(kill.killed);
    super.assertTrue(kill.restart);

    try {
      db.getActiveProcesses().getProcess(proc.getProcessID());
      throw new Exception("Process not removed from db");
    } catch (LogicException e) {
      //ok
    }
  }

  public void testRestartDenied() throws Exception {
    ProcessDB        db   = new TestProcessDB();
    DistributionInfo dist = new DistributionInfo("test", "1.0", "test", "testVm");
    Process          proc = new Process(dist);
    db.getActiveProcesses().addProcess(proc);
    Thread.sleep(500);

    TestKill kill = new TestKill(Process.KILL_REQUESTOR_SERVER, db,
                                 proc.getProcessID(), 5000L);
    proc.confirmKilled();
    _tm.execSyncTask("kill", kill);
    super.assertTrue(kill.killed);
    super.assertTrue(kill.restart == false);

    try {
      db.getActiveProcesses().getProcess(proc.getProcessID());
      throw new Exception("Process not removed from db");
    } catch (LogicException e) {
      //ok
    }
  }

  static class TestKill extends KillTask {
    boolean killed;
    boolean restart;

    TestKill(String requestor, ProcessDB db, String vmId, int maxRetry) throws Exception{
      super(new TCPAddress("localhost", 33000), 8080, requestor, vmId, db, maxRetry, 500, new TestPortManager());
    }
    
    TestKill(String requestor, ProcessDB db, String vmId) throws Exception{
      super(new TCPAddress("localhost", 33000), 8080, requestor, vmId, db, 3, 500, new TestPortManager());
    }
    
    TestKill(String requestor, ProcessDB db, String vmId, long restartInterval) throws Exception{
      super(new TCPAddress("localhost", 33000), 8080, requestor, vmId, db, 3, restartInterval, new TestPortManager());
    }    
    
    /**
     * @see org.sapia.corus.processor.task.KillTask#onKillConfirmed(org.sapia.taskman.TaskContext)
     */
    protected void onKillConfirmed(TaskContext ctx) {
      super.onKillConfirmed(ctx);
      killed = true;
    }
    
    /**
     * @see org.sapia.corus.processor.task.KillTask#onMaxRetry(org.sapia.taskman.TaskContext)
     */
    protected boolean onMaxRetry(TaskContext ctx) {
      super.onMaxRetry(ctx);
      killed = true;
      return true;
    }
    
    /**
     * @see org.sapia.corus.processor.task.KillTask#onRestarted()
     */
    protected void onRestarted() {
      restart = true;
    }
    
  }
}
