package org.sapia.corus.processor.task;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.deployer.DistributionDatabase;

/**
 * @author Yanick Duchesne
 */
public class RestartTaskTest extends BaseTaskTest{
  /**
   * @param arg0
   */
  public RestartTaskTest(String arg0) {
    super(arg0);
  }
  
  public void testRestart() throws Exception{
    Distribution dist = new Distribution();
    dist.setName("test");
    dist.setVersion("1.0");
    ProcessConfig conf = new ProcessConfig();
    conf.setName("testVm");
    dist.addProcess(conf);
    DistributionDatabase store = ctx.getServices().getDistributions();
    store.addDistribution(dist);
    DistributionInfo info = new DistributionInfo("test", "1.0", "test", "testVm");
    Process          proc = new Process(info);
    db.getActiveProcesses().addProcess(proc);
    RestartTask restart = new RestartTask(
        ProcessTerminationRequestor.KILL_REQUESTOR_PROCESS, proc.getProcessID(), 3);
    tm.executeAndWait(restart);
    proc.confirmKilled();    
    tm.executeAndWait(restart);    
  
  }

}
