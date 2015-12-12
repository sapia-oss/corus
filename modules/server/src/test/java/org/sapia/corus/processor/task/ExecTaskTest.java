package org.sapia.corus.processor.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.Port;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.ProcessStartupInfo;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.processor.hook.ProcessHookManager;
import org.sapia.corus.taskmanager.core.TaskParams;

public class ExecTaskTest extends TestBaseTask{

  private Distribution  dist;
  private ProcessConfig conf;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    dist = super.createDistribution("testDist", "1.0");
    conf = super.createProcessConfig(dist, "testProc", "testProfile");
  }

  @Test
  public void testExecuteProcess() throws Exception{
    ExecTask task = new ExecTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(dist, conf, "testProfile", ProcessStartupInfo.forSingleProcess())).get();
    assertEquals(1, ctx.getProc().getProcesses(ProcessCriteria.builder().all()).size());
  }

  @Test
  public void testExecuteProcessWithPort() throws Exception{
    PortRange range = new PortRange("test", 8080, 8080);
    ctx.getPorts().addPortRange(range);
    Port port = conf.createPort();
    port.setName("test");

    ExecTask task = new ExecTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(dist, conf, "testProfile", ProcessStartupInfo.forSingleProcess())).get();
    assertEquals(1, ctx.getProc().getProcesses(ProcessCriteria.builder().all()).size());
    range = ctx.getPorts().getPortRanges().get(0);
    assertEquals(1, range.getActive().size());
    assertEquals(0, range.getAvailable().size());
  }

  @Test
  public void testExecuteProcessWithPortFailed() throws Exception{
    ProcessHookManager hooks = mock(ProcessHookManager.class);
    ctx.getServices().rebind(ProcessHookManager.class, hooks);
    doThrow(new IOException("Could not start process"))
      .when(hooks).start(any(ProcessContext.class), any(StarterResult.class), any(LogCallback.class));

    PortRange range = new PortRange("test", 8080, 8080);
    ctx.getPorts().addPortRange(range);
    Port port = conf.createPort();
    port.setName("test");

    ExecTask task = new ExecTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(dist, conf, "testProfile", ProcessStartupInfo.forSingleProcess())).get();
    assertEquals(0, ctx.getProc().getProcesses(ProcessCriteria.builder().all()).size());
    range = ctx.getPorts().getPortRanges().get(0);
    assertEquals(0, range.getActive().size());
    assertEquals(1, range.getAvailable().size());
  }

  @Test
  public void testExecuteProcessWithNuma() throws Exception{
    ctx.getNumaModule().setEnabled(true);
    ctx.getNumaModule().setFirstNumaNodeId(2);
    ctx.getNumaModule().setNumaNodeCount(4);

    ExecTask task = new ExecTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(dist, conf, "testProfile", ProcessStartupInfo.forSingleProcess())).get();
    assertEquals(1, ctx.getProc().getProcesses(ProcessCriteria.builder().all()).size());
    assertEquals(1, ctx.getNumaModule().getProcessBindings().size());
    assertEquals(2, ctx.getNumaModule().getProcessBindings().entrySet().iterator().next().getValue().intValue());
  }

  @Test
  public void testExecuteProcessWithNumaFailed() throws Exception{
    ProcessHookManager hooks = mock(ProcessHookManager.class);
    ctx.getServices().rebind(ProcessHookManager.class, hooks);
    doThrow(new IOException("Could not start process"))
      .when(hooks).start(any(ProcessContext.class), any(StarterResult.class), any(LogCallback.class));

    ctx.getNumaModule().setEnabled(true);
    ctx.getNumaModule().setFirstNumaNodeId(1);
    ctx.getNumaModule().setNumaNodeCount(4);

    ExecTask task = new ExecTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(dist, conf, "testProfile", ProcessStartupInfo.forSingleProcess())).get();
    assertEquals(0, ctx.getProc().getProcesses(ProcessCriteria.builder().all()).size());
    assertEquals(0, ctx.getNumaModule().getProcessBindings().size());
  }

}
