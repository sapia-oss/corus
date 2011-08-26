package org.sapia.corus.processor.task;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.taskmanager.core.TaskParams;

public class SuspendTaskTest extends TestBaseTask{

  private Process       proc;

  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    Distribution dist  = super.createDistribution("testDist", "1.0");
    ProcessConfig conf  = super.createProcessConfig(dist, "testProc", "testProfile");
    proc = super.createProcess(dist, conf, "testProfile");
    proc.setMaxKillRetry(1);
    proc.save();
  }

  @Test
  public void testExecute() throws Exception{
    proc.confirmKilled();
    proc.save();
    SuspendTask suspend = new SuspendTask(proc.getMaxKillRetry());
    TaskParams<Process, ProcessTerminationRequestor, Void, Void> params = TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER);
    tm.executeAndWait(suspend, params).get();
    proc.getLock().awaitRelease(10, TimeUnit.SECONDS);
    assertEquals(LifeCycleStatus.SUSPENDED, proc.getStatus());
    assertTrue("Process should be in suspended list", ctx.getServices().getProcesses().getSuspendedProcesses().containsProcess(proc.getProcessID()));    
  }  
}
