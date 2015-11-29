package org.sapia.corus.processor.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.diagnostic.DiagnosticModule;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticStatus;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.event.ProcessStaleEvent;

/**
 * @author Yanick Duchesne
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessCheckTaskTest extends TestBaseTask {

  @Mock
  private OsModule os;
  
  @Mock
  private DiagnosticModule diagnostics;
  
  @Mock
  private EventDispatcher dispatcher;
  
  private Process proc;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    ctx.getServices().rebind(DiagnosticModule.class, diagnostics);
    ctx.getServices().rebind(OsModule.class, os);
    ctx.getServices().rebind(EventDispatcher.class, dispatcher);

    Distribution  dist  = super.createDistribution("testDist", "1.0");
    ProcessConfig conf  = super.createProcessConfig(dist, "testProc", "testProfile");
    proc = super.createProcess(dist, conf, "testProfile");
    proc.setMaxKillRetry(1);
    proc.save();
  }
  
  @Test
  public void testStaleVmCheck() throws Exception {   
    ctx.getProc().getConfigurationImpl().setProcessTimeout(1);
    ctx.getProc().getConfigurationImpl().setKillInterval(1);
    
    LockOwner owner = LockOwner.createInstance().nonExclusive();
    proc.getLock().acquire(owner);
    proc.save();
    Thread.sleep(1100);
    ProcessCheckTask task = new ProcessCheckTask();
    ctx.getTm().executeAndWait(task, null).get();
    
    proc.getLock().awaitRelease(5, TimeUnit.SECONDS);
    
    assertFalse(
        "Process should have been removed from active process list", 
        ctx.getServices().getProcesses().containsProcess(proc.getProcessID())
    );
  }
  
  @Test
  public void testStaleVmCheck_diagnostic_success() throws Exception {   
    proc.setInteropEnabled(false);
    when(diagnostics.acquireProcessDiagnostics(any(Process.class), any(OptionalValue.class)))
      .thenReturn(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_SUCCESSFUL, "success", proc));
    
    ctx.getProc().getConfigurationImpl().setProcessTimeout(1);
    ctx.getProc().getConfigurationImpl().setKillInterval(1);
    
    LockOwner owner = LockOwner.createInstance().nonExclusive();
    proc.getLock().acquire(owner);
    proc.save();
    Thread.sleep(1100);
    ProcessCheckTask task = new ProcessCheckTask();
    ctx.getTm().executeAndWait(task, null).get();
    
    proc.getLock().awaitRelease(5, TimeUnit.SECONDS);
    
    assertTrue(
        "Process should NOT have been removed from active process list", 
        ctx.getServices().getProcesses().containsProcess(proc.getProcessID())
    );
  }
  
  @Test
  public void testStaleVmCheck_diagnostic_failure() throws Exception {   
    proc.setInteropEnabled(false);
    when(diagnostics.acquireProcessDiagnostics(any(Process.class), any(OptionalValue.class)))
      .thenReturn(new ProcessDiagnosticResult(ProcessDiagnosticStatus.CHECK_FAILED, "failure", proc));
    
    ctx.getProc().getConfigurationImpl().setProcessTimeout(1);
    ctx.getProc().getConfigurationImpl().setKillInterval(1);
    
    LockOwner owner = LockOwner.createInstance().nonExclusive();
    proc.getLock().acquire(owner);
    proc.save();
    Thread.sleep(1100);
    ProcessCheckTask task = new ProcessCheckTask();
    ctx.getTm().executeAndWait(task, null).get();
    
    proc.getLock().awaitRelease(5, TimeUnit.SECONDS);
    
    assertFalse(
        "Process should have been removed from active process list", 
        ctx.getServices().getProcesses().containsProcess(proc.getProcessID())
    );
  }
  
  @Test
  public void testStaleVmCheckNoAutoRestart() throws Exception {

    super.processorConf.setAutoRestart(false);
    ctx.getProc().getConfigurationImpl().setProcessTimeout(1);
    ctx.getProc().getConfigurationImpl().setKillInterval(1);

    Thread.sleep(1100);
    ProcessCheckTask task = new ProcessCheckTask();
    ctx.getTm().executeAndWait(task, null).get();
    
    assertTrue(
        "Process should not have been removed from active process list", 
        ctx.getServices().getProcesses().containsProcess(proc.getProcessID())
    );
    
    verify(dispatcher).dispatch(any(ProcessStaleEvent.class));
    verify(os, never()).killProcess(any(LogCallback.class), eq(KillSignal.SIGKILL), anyString());
    
  }  
  
  @Test
  public void testStaleVmCheckNoAutoRestart_specific_proc_timeout() throws Exception {
    
    proc.setPollTimeout(1);
    
    super.processorConf.setAutoRestart(false);
    ctx.getProc().getConfigurationImpl().setProcessTimeout(2);
    ctx.getProc().getConfigurationImpl().setKillInterval(1);

    Thread.sleep(1100);
    ProcessCheckTask task = new ProcessCheckTask();
    ctx.getTm().executeAndWait(task, null).get();
    
    assertTrue(
        "Process should not have been removed from active process list", 
        ctx.getServices().getProcesses().containsProcess(proc.getProcessID())
    );
    
    verify(dispatcher).dispatch(any(ProcessStaleEvent.class));
    verify(os, never()).killProcess(any(LogCallback.class), eq(KillSignal.SIGKILL), anyString());
    
  }  
  
}
