package org.sapia.corus.processor.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.pub.ProcessPublisher;
import org.sapia.corus.taskmanager.core.TaskParams;

@RunWith(MockitoJUnitRunner.class)
public class SuspendTaskTest extends TestBaseTask {

  private Process       proc;

  @Mock
  private ProcessPublisher  publisher;
  
  @Mock
  private OsModule os;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    ctx.getServices().rebind(OsModule.class, os);
    ctx.getServices().rebind(ProcessPublisher.class, publisher);

    final Distribution dist  = super.createDistribution("testDist", "1.0");
    final ProcessConfig conf  = super.createProcessConfig(dist, "testProc", "testProfile");
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
    
    ProcessCriteria suspCriteria = ProcessCriteria.builder().pid(ArgMatchers.exact(proc.getProcessID())).lifecycles(LifeCycleStatus.SUSPENDED).build();

    assertFalse("Process should be in suspended list", ctx.getServices().getProcesses().getProcesses(suspCriteria).isEmpty());

    verify(os).killProcess(any(OsModule.LogCallback.class), eq(KillSignal.SIGKILL), anyString());    
  }  
}
