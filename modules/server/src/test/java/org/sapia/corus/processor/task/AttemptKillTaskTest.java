package org.sapia.corus.processor.task;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.processor.hook.ProcessHookManager;
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
    boolean completed = ctx.getTm().executeAndWait(task, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 1, 3)).get();
    assertFalse("Process has not confirmed termination; task should not be completed", completed);
  }
  
  @Test
  public void testExecuteKill_SIGTERM() throws Exception{
    ProcessHookManager processHooks = ctx.getServices().lookup(ProcessHookManager.class);
    
    AttemptKillTask task = new AttemptKillTask();
    boolean completed = ctx.getTm().executeAndWait(task, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 2, 3)).get();
    assertFalse("Process has not confirmed termination; task should not be completed", completed);
    verify(processHooks).kill(any(ProcessContext.class), eq(KillSignal.SIGTERM), any(LogCallback.class));    
  }
  
  @Test
  public void testExecuteKill_SIGTERM_with_error() throws Exception{
    ProcessHookManager processHooks = ctx.getServices().lookup(ProcessHookManager.class);
    doThrow(new IOException("Error!!!")).when(processHooks).kill(any(), any(), any());
    
    AttemptKillTask task = new AttemptKillTask();
    boolean completed = ctx.getTm().executeAndWait(task, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 2, 3)).get();
    assertTrue("Process kill error: KILL_ASSUMED status should be set", completed);
    
    assertEquals(LifeCycleStatus.KILL_ASSUMED, proc.getStatus());
    verify(processHooks).kill(any(ProcessContext.class), eq(KillSignal.SIGTERM), any(LogCallback.class));    
  }
  
  @Test
  public void testExecuteKillConfirmed() throws Exception{
    AttemptKillTask task = new AttemptKillTask();
    proc.setStatus(LifeCycleStatus.KILL_CONFIRMED);
    boolean completed = ctx.getTm().executeAndWait(task, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 1, 3)).get();
    assertTrue("Process has confirmed termination; task should be completed", completed);
  }
  
  @Test
  public void testExecuteKillAssumed() throws Exception{
    AttemptKillTask task = new AttemptKillTask();
    proc.setStatus(LifeCycleStatus.KILL_ASSUMED);
    boolean completed = ctx.getTm().executeAndWait(task, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, 1, 3)).get();
    assertTrue("Process is assumed terminated; task should be completed", completed);
  }
}
