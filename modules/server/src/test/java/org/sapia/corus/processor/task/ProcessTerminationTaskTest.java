package org.sapia.corus.processor.task;

import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;


/**
 * @author Yanick Duchesne
 *
 */
public class ProcessTerminationTaskTest extends BaseTaskTest{
  
  public ProcessTerminationTaskTest(String name) {
    super(name);
  }
  
  
  public void testExecCount() throws Exception{
    DistributionInfo dist = new DistributionInfo("test", "1.0", "test", "testVm");
    Process          proc = new Process(dist);
    db.getActiveProcesses().addProcess(proc);

    TestProcessTerminationTask task = new TestProcessTerminationTask(
                                                                     ProcessTerminationRequestor.KILL_REQUESTOR_PROCESS,
                                                                     proc.getProcessID(),
                                                                     3);
    tm.executeAndWait(task).get();
    tm.executeAndWait(task).get();
    tm.executeAndWait(task).get();
    tm.executeAndWait(task).get();
    tm.executeAndWait(task).get();

    super.assertEquals(3, task.onExec);
    super.assertEquals(2, task.onMaxRetry);
  }
}
