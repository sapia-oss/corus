package org.sapia.corus.processor.task;

import org.sapia.corus.admin.services.processor.DistributionInfo;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.admin.services.processor.Processor;
import org.sapia.corus.admin.services.processor.ProcessorConfigurationImpl;
import org.sapia.corus.processor.TestProcessor;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.util.PropertyFactory;

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
    proc.poll();
    db.getActiveProcesses().addProcess(proc);
    
    TestProcessor processor = (TestProcessor)ctx.lookup(Processor.class);
    ProcessorConfigurationImpl processorConf = (ProcessorConfigurationImpl)processor.getConfiguration();
    processorConf.setProcessTimeout(PropertyFactory.create(1));
    
    Thread.sleep(1500);

    TestVmCheck t = new TestVmCheck();
   // proc.confirmKilled();
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
