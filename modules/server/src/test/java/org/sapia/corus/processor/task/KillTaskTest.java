package org.sapia.corus.processor.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.exceptions.processor.ProcessLockException;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.Port;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.client.services.processor.event.ProcessKillPendingEvent;
import org.sapia.corus.client.services.processor.event.ProcessKilledEvent;
import org.sapia.corus.client.services.processor.event.ProcessRestartPendingEvent;
import org.sapia.corus.client.services.processor.event.ProcessRestartedEvent;
import org.sapia.corus.client.services.pub.ProcessPublisher;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.processor.hook.ProcessHookManager;
import org.sapia.corus.taskmanager.core.TaskParams;

/**
 * @author Yanick Duchesne
 */
@RunWith(MockitoJUnitRunner.class)
public class KillTaskTest extends TestBaseTask {
  
  private Process          proc;

  @Mock
  private EventDispatcher  dispatcher;
  
  @Mock
  private ProcessPublisher publisher;
  
  @Mock
  private ProcessHookManager processHooks;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    final Distribution  dist  = super.createDistribution("testDist", "1.0");
    final ProcessConfig conf  = super.createProcessConfig(dist, "testProc", "testProfile");
    super.ctx.getServices().rebind(EventDispatcher.class, dispatcher);
    super.ctx.getServices().rebind(ProcessPublisher.class, publisher);
    super.ctx.getServices().rebind(ProcessHookManager.class, processHooks);
    PortRange     range = new PortRange("test", 8080, 8080);
    ctx.getPorts().addPortRange(range);
    Port port = conf.createPort();
    port.setName("test");
    proc = super.createProcess(dist, conf, "testProfile");
    int portNumber = ctx.getPorts().aquirePort(port.getName());
    final ActivePort ap = new ActivePort(port.getName(), portNumber);
    proc.addActivePort(ap);
  }

  @Test
  public void testKillFromCorusNotConfirmed() throws Exception {
    KillTask kill = new KillTask(3);  
    tm.executeAndWait(kill, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)).get();
    assertTrue(
        "Process should not have been killed", 
        ctx.getProc().getProcessDB().containsProcess(proc.getProcessID())
    );
    
    verify(dispatcher).dispatch(isA(ProcessKillPendingEvent.class));
  }

  @Test
  public void testKillFromCorus_zeroRetry() throws Exception {
    KillTask kill = new KillTask(0);  
    tm.executeAndWait(kill, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)).get();
    assertTrue(
        "Process should not have been killed", 
        ctx.getProc().getProcessDB().containsProcess(proc.getProcessID())
    );
    verify(dispatcher).dispatch(isA(ProcessKillPendingEvent.class));
  }

  @Test
  public void testKillFromCorusConfirmed() throws Exception {

    proc.confirmKilled();
    proc.save();
    KillTask kill = new KillTask(3);  
    tm.executeAndWait(kill, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN)).get();
    
    assertFalse(
        "Process should have been killed", 
        ctx.getProc().getProcessDB().containsProcess(proc.getProcessID())
    );
    assertEquals("Port should have been released", 1, ctx.getPorts().getPortRanges().get(0).getAvailable().size());
    
    verify(dispatcher).dispatch(isA(ProcessKillPendingEvent.class));
    verify(dispatcher).dispatch(isA(ProcessKilledEvent.class));
    verify(processHooks).kill(any(ProcessContext.class), eq(KillSignal.SIGKILL), any(LogCallback.class));
  }

  @Test
  public void testKill_zeroRetryAttempt() throws Exception {
    int      maxRetry = 0;
    KillTask kill        = new KillTask(maxRetry);
    for(int i = 0; i < 1+maxRetry; i++){
      tm.executeAndWait(
          kill, 
          TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN)
      ).get();
    }
    
    assertTrue(
        "Process should not have been killed", 
        ctx.getProc().getProcessDB().containsProcess(proc.getProcessID())
    );

    // ultimate attempt
    tm.executeAndWait(
        kill, 
        TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN)
    ).get();
    
    assertFalse(
        "Process should have been killed and not restarted", 
        ctx.getProc().getProcessDB().containsProcess(proc.getProcessID())
    );
    
    verify(dispatcher).dispatch(isA(ProcessKillPendingEvent.class));
    verify(dispatcher).dispatch(isA(ProcessKilledEvent.class));
    verify(dispatcher, never()).dispatch(isA(ProcessRestartPendingEvent.class));
    verify(dispatcher, never()).dispatch(isA(ProcessRestartedEvent.class));
    verify(processHooks).kill(any(ProcessContext.class), eq(KillSignal.SIGKILL), any(LogCallback.class));
  }

  @Test
  public void testKillMaxAttemptReachedNoRestart() throws Exception {
    int      maxRetry = 3;
    KillTask kill        = new KillTask(maxRetry);
    for(int i = 0; i < 1+maxRetry; i++){
      tm.executeAndWait(
          kill, 
          TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN)
      ).get();
    }
    
    assertTrue(
        "Process should not have been killed", 
        ctx.getProc().getProcessDB().containsProcess(proc.getProcessID())
    );

    // ultimate attempt
    tm.executeAndWait(
        kill, 
        TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN)
    ).get();
    
    assertFalse(
        "Process should have been killed and not restarted", 
        ctx.getProc().getProcessDB().containsProcess(proc.getProcessID())
    );
    
    verify(dispatcher).dispatch(isA(ProcessKillPendingEvent.class));
    verify(dispatcher).dispatch(isA(ProcessKilledEvent.class));
    verify(dispatcher, never()).dispatch(isA(ProcessRestartPendingEvent.class));
    verify(dispatcher, never()).dispatch(isA(ProcessRestartedEvent.class));
    verify(processHooks).kill(any(ProcessContext.class), eq(KillSignal.SIGKILL), any(LogCallback.class));
  }

  @Test
  public void testKillMaxAttemptReachedWithRestart() throws Exception {
    int      maxRetry = 3;
    String   oldPid = proc.getOsPid();
    ctx.getProc().getConfigurationImpl().setRestartInterval(0);
    KillTask kill        = new KillTask(maxRetry);
    for(int i = 0; i < 1+maxRetry; i++){
      tm.executeAndWait(
          kill, 
          TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)
      ).get();
    }
    
    assertTrue(
        "Process should not have been killed", 
        ctx.getProc().getProcessDB().containsProcess(proc.getProcessID())
    );

    // ultimate attempt
    tm.executeAndWait(
        kill, 
        TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)
    ).get();
    
    assertTrue(
        "Process should have been killed and restarted", 
        ctx.getProc().getProcessDB().containsProcess(proc.getProcessID())
    );
    
    Process restarted = ctx.getProc().getProcessDB().getProcess(proc.getProcessID());
    assertNotSame("Restarted process should have new PID", oldPid, restarted.getOsPid());
    
    verify(dispatcher).dispatch(isA(ProcessKillPendingEvent.class));
    verify(dispatcher).dispatch(isA(ProcessKilledEvent.class));
    verify(dispatcher).dispatch(isA(ProcessRestartPendingEvent.class));
    verify(dispatcher).dispatch(isA(ProcessRestartedEvent.class));
  }

  @Test
  public void testRestartDenied() throws Exception {
    proc.confirmKilled();
    proc.save();
    KillTask kill = new KillTask(3);
    tm.executeAndWait(
          kill, 
          TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)
    ).get();
    
    assertFalse(
        "Process should not have been restarted", 
        ctx.getProc().getProcessDB().containsProcess(proc.getProcessID())
    );
    
    verify(dispatcher).dispatch(isA(ProcessKillPendingEvent.class));
    verify(dispatcher).dispatch(isA(ProcessKilledEvent.class));
    verify(dispatcher, never()).dispatch(isA(ProcessRestartPendingEvent.class));
    verify(dispatcher, never()).dispatch(isA(ProcessRestartedEvent.class));
  }
  
  @Test
  public void testHardKill() throws Exception {    
    KillTask kill = new KillTask(3);  
    kill.setHardKill(true);
    tm.executeAndWait(kill, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN)).get();

    assertFalse(
        "Process should not have been killed", 
        ctx.getProc().getProcessDB().containsProcess(proc.getProcessID())
    );
    
    verify(processHooks).kill(any(ProcessContext.class), eq(KillSignal.SIGKILL), any(LogCallback.class));
    verify(dispatcher).dispatch(isA(ProcessKillPendingEvent.class));
    verify(dispatcher).dispatch(isA(ProcessKilledEvent.class));
    verify(dispatcher, never()).dispatch(isA(ProcessRestartPendingEvent.class));
    verify(dispatcher, never()).dispatch(isA(ProcessRestartedEvent.class));
  }
  
  @Test(expected=ProcessLockException.class)
  public void testConcurrentAccess() throws Exception{
    KillTask kill = new KillTask(3);
    tm.executeAndWait(
          kill, 
          TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)
    ).get();
    
    proc.getLock().acquire(LockOwner.createInstance());
    
    verify(dispatcher, never()).dispatch(isA(ProcessKillPendingEvent.class));
    verify(dispatcher, never()).dispatch(isA(ProcessKilledEvent.class));
    verify(dispatcher, never()).dispatch(isA(ProcessRestartPendingEvent.class));
    verify(dispatcher, never()).dispatch(isA(ProcessRestartedEvent.class));
  }

}
