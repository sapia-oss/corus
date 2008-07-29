package org.sapia.corus.processor.task;

import org.sapia.corus.port.TestPortManager;
import org.sapia.corus.processor.*;
import org.sapia.corus.processor.Process;

import org.sapia.taskman.TaskContext;
import org.sapia.ubik.net.TCPAddress;


/**
 * @author Yanick Duchesne
 * 2002-03-03
 */
public class ProcessCheckTaskTest extends BaseTaskTest {
  
  /**
   * Constructor for VmCheckTaskTest.
   * @param arg0
   */
  public ProcessCheckTaskTest(String arg0) {
    super(arg0);
  }
  
  public void testStaleVmCheck() throws Exception {
    ProcessDB        db   = new TestProcessDB();
    DistributionInfo dist = new DistributionInfo("test", "1.0", "test", "testVm");
    Process          proc = new Process(dist);
    proc.poll();
    db.getActiveProcesses().addProcess(proc);
    Thread.sleep(1500);

    TestVmCheck t = new TestVmCheck(db, 1);
   // proc.confirmKilled();
    _tm.execSyncTask("check", t);
    super.assertTrue(t.killed);
  }

  static class TestVmCheck extends ProcessCheckTask {
    boolean killed;
    
    /**
     * @see org.sapia.corus.processor.task.ProcessCheckTask#exec(org.sapia.taskman.TaskContext)
     */
    public void exec(TaskContext ctx) {
      super.exec(ctx);
    }

    public TestVmCheck(ProcessDB db, int vmTimeout) throws Exception{
      super(new TCPAddress("localhost", 33000), 8080, db, 0, vmTimeout, 120 * 1000, new TestPortManager());
    }
    
    /**
     * @see org.sapia.corus.processor.task.ProcessCheckTask#onTimeout()
     */
    protected void onTimeout() {
      killed = true;
    }
 
  }
}
