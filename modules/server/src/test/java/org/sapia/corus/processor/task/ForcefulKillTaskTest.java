package org.sapia.corus.processor.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.Port;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.processor.hook.ProcessHookManager;
import org.sapia.corus.taskmanager.core.TaskParams;

@RunWith(MockitoJUnitRunner.class)
public class ForcefulKillTaskTest extends TestBaseTask{

  private Process          proc;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    
    Distribution  dist  = super.createDistribution("testDist", "1.0");
    ProcessConfig conf  = super.createProcessConfig(dist, "testProc", "testProfile");
    PortRange     range = new PortRange("test", 8080, 8080);
    ctx.getPorts().addPortRange(range);
    Port port = conf.createPort();
    port.setName("test");
    proc = super.createProcess(dist, conf, "testProfile");
    int portNumber = ctx.getPorts().aquirePort(port.getName());
    proc.addActivePort(new ActivePort(port.getName(), portNumber));
  }

  @Test
  public void testExecute() throws Exception{
    ForcefulKillTask task = new ForcefulKillTask();
    boolean actual = ctx.getTm().executeAndWait(task, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)).get(); 

    assertTrue("Kill should have been successful", actual); 
    assertEquals(LifeCycleStatus.KILL_CONFIRMED, ctx.getProc().getProcess(proc.getProcessID()).getStatus());
  }
  @Test
  public void testExecute_with_failure() throws Exception{
    ProcessHookManager processHooks = ctx.getServices().lookup(ProcessHookManager.class);
    doThrow(new IOException("Kill error")).when(processHooks).kill(
          any(ProcessContext.class), 
          any(KillSignal.class),
          any(LogCallback.class));
    ForcefulKillTask task = new ForcefulKillTask();
    boolean actual = ctx.getTm().executeAndWait(task, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)).get(); 

    assertTrue("Kill should have been successful", actual);
    assertEquals(LifeCycleStatus.KILL_ASSUMED, ctx.getProc().getProcess(proc.getProcessID()).getStatus());
  } 
  
}
