package org.sapia.corus.processor.task;

import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.processor.ProcessorConfigurationImpl;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * @author Yanick Duchesne
 */
public class ProcessCheckTaskTest extends BaseTaskTest {
  
  public ProcessCheckTaskTest(String arg0) {
    super(arg0);
  }
  
  public void testStaleVmCheck() throws Exception {
    DistributionInfo dist = new DistributionInfo("test", "1.0", "test", "testVm");
    Process          proc = new Process(dist);
    db.getActiveProcesses().addProcess(proc);
    proc.touch();
    proc.save();
    
    ProcessorConfigurationImpl processorConf = (ProcessorConfigurationImpl) ctx.getProc().getConfiguration();
    processorConf.setProcessTimeout(1);
    
    Thread.sleep(1500);

    TestVmCheck t = new TestVmCheck();
    //proc.confirmKilled();
    tm.executeAndWait(t).get();
    super.assertTrue(t.killed);
  }

  static class TestVmCheck extends ProcessCheckTask {
    boolean killed;

    
    @Override
    protected void onTimeout(TaskExecutionContext ctx) {
      killed = true;
    }
 
  }
}
