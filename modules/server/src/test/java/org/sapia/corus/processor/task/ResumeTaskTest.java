package org.sapia.corus.processor.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.taskmanager.core.TaskParams;

public class ResumeTaskTest extends TestBaseTask{

  private Distribution dist;
  private ProcessConfig conf;
  private Process  proc;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    dist  = super.createDistribution("testDist", "1.0");
    conf  = super.createProcessConfig(dist, "testProc", "testProfile");
    proc = super.createProcess(dist, conf, "testProfile");
    proc.setStatus(LifeCycleStatus.SUSPENDED);
    proc.save();
  }
  
  @Test
  public void testExecute() throws Exception{
    ResumeTask task = new ResumeTask();
    proc.getDistributionInfo();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(proc, dist, conf)).get();
    proc.getLock().awaitRelease(10, TimeUnit.SECONDS);
    
    ProcessCriteria suspCriteria = ProcessCriteria.builder().pid(ArgMatchers.exact(proc.getProcessID())).lifecycles(LifeCycleStatus.SUSPENDED).build();
    assertTrue("Process should not be in suspended process list", ctx.getServices().getProcesses().getProcesses(suspCriteria).isEmpty());
    
    ProcessCriteria actCriteria = ProcessCriteria.builder().pid(ArgMatchers.exact(proc.getProcessID())).lifecycles(LifeCycleStatus.ACTIVE).build();
    assertFalse("Process should be in active process list", ctx.getServices().getProcesses().getProcesses(actCriteria).isEmpty());
  }

}
