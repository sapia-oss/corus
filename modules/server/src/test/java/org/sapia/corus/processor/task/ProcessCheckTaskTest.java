package org.sapia.corus.processor.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.os.OsModule.LogCallback;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.event.ProcessStaleEvent;

/**
 * @author Yanick Duchesne
 */
public class ProcessCheckTaskTest extends TestBaseTask {

  private Process       proc;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    Distribution  dist  = super.createDistribution("testDist", "1.0");
    ProcessConfig conf  = super.createProcessConfig(dist, "testProc", "testProfile");
    proc = super.createProcess(dist, conf, "testProfile");
    proc.setMaxKillRetry(1);
    proc.save();
  }
  
  @Test
  public void testStaleVmCheck() throws Exception {   
    OsModule os = mock(OsModule.class);
    ctx.getServices().rebind(OsModule.class, os);
    
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
    OsModule os = mock(OsModule.class);
    EventDispatcher dispatcher = mock(EventDispatcher.class);
    ctx.getServices().rebind(OsModule.class, os);
    ctx.getServices().rebind(EventDispatcher.class, dispatcher);
    
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
    
    OsModule os = mock(OsModule.class);
    EventDispatcher dispatcher = mock(EventDispatcher.class);
    ctx.getServices().rebind(OsModule.class, os);
    ctx.getServices().rebind(EventDispatcher.class, dispatcher);
    
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
