package org.sapia.corus.processor.task;

import junit.framework.TestCase;

import org.sapia.corus.processor.DistributionInfo;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.processor.TestProcessDB;
import org.sapia.taskman.NullTaskManager;
import org.sapia.taskman.TaskManager;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ProcessTerminationTaskTest extends TestCase {
  
  TaskManager _tm;  
  
  public ProcessTerminationTaskTest(String name) {
    super(name);
  }
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    _tm   = new NullTaskManager();
  }
  
  public void testExecCount() throws Exception{
    ProcessDB        db   = new TestProcessDB();
    DistributionInfo dist = new DistributionInfo("test", "1.0", "test", "testVm");
    Process          proc = new Process(dist);
    db.getActiveProcesses().addProcess(proc);

    TestProcessTerminationTask task = new TestProcessTerminationTask(Process.KILL_REQUESTOR_PROCESS,
                                                                     proc.getProcessID(),
                                                                     db, 3);
    _tm.execSyncTask("terminate", task);
    _tm.execSyncTask("terminate", task);
    _tm.execSyncTask("terminate", task);
    _tm.execSyncTask("terminate", task);
    _tm.execSyncTask("terminate", task);

    super.assertEquals(3, task.onExec);
    super.assertEquals(1, task.onMaxRetry);
  }
}
