package org.sapia.corus.processor.task;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.taskmanager.core.TaskParams;

public class AttemptKillTaskTest extends TestBaseTask{

  private Process          proc;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    Distribution  dist = super.createDistribution("testDist", "1.0");
    ProcessConfig conf = super.createProcessConfig(dist, "testProc", "testProfile");
    proc = super.createProcess(dist, conf, "testProfile");
  }
  
  @Test
  public void testExecuteKillNotConfirmed() throws Exception{
    AttemptKillTask task = new AttemptKillTask();
    boolean completed = ctx.getTm().executeAndWait(task, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 1)).get();
    assertFalse("Process has not confirmed termination; task should not be completed", completed);
  }
  
  @Test
  public void testExecuteKillConfirmed() throws Exception{
    AttemptKillTask task = new AttemptKillTask();
    proc.setStatus(LifeCycleStatus.KILL_CONFIRMED);
    boolean completed = ctx.getTm().executeAndWait(task, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 1)).get();
    assertTrue("Process has confirmed termination; task should be completed", completed);
  }
}
