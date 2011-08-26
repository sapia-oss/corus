package org.sapia.corus.processor.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.Port;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.taskmanager.core.TaskParams;

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
    assertEquals("Should not have available ports", 0, ctx.getPorts().getPortRanges().get(0).getAvailable().size());
    assertTrue(
        "Kill should have been successful", 
        ctx.getTm().executeAndWait(task, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)).get()
    );
    assertEquals("Should have available port", 1, ctx.getPorts().getPortRanges().get(0).getAvailable().size());
  }
  
  @Test
  public void testExecuteFailure() throws Exception{
    OsModule os = mock(OsModule.class);
    doThrow(new IOException("Kill error")).when(os).killProcess(
          any(OsModule.LogCallback.class), 
          any(String.class));
    ctx.getServices().rebind(OsModule.class, os);
    ForcefulKillTask task = new ForcefulKillTask();
    assertEquals("Should not have available ports", 0, ctx.getPorts().getPortRanges().get(0).getAvailable().size());
    assertFalse(
        "Kill should not have been successful", 
        ctx.getTm().executeAndWait(task, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)).get()
    );
    assertEquals("Should have available port", 1, ctx.getPorts().getPortRanges().get(0).getAvailable().size());
  } 
}
