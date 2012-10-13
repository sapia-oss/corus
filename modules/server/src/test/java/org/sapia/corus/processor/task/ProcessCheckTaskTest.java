package org.sapia.corus.processor.task;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.os.OsModule.LogCallback;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.event.ProcessStaleEvent;

/**
 * @author Yanick Duchesne
 */
public class ProcessCheckTaskTest extends TestBaseTask {

  private Process  proc;
  
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
    final CountDownLatch latch = new CountDownLatch(1);
    
    OsModule os = mock(OsModule.class);
    ctx.getServices().rebind(OsModule.class, os);
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        latch.countDown();
        return null;
      }
    }).when(os).killProcess(any(OsModule.LogCallback.class), any(String.class));
    
    ctx.getProc().getConfigurationImpl().setProcessTimeout(1);
    ctx.getProc().getConfigurationImpl().setKillInterval(1);

    Thread.sleep(1100);
    ProcessCheckTask task = new ProcessCheckTask();
    ctx.getTm().executeAndWait(task, null).get();
    latch.await(10, TimeUnit.SECONDS);
    proc.getLock().awaitRelease(10, TimeUnit.SECONDS);
   
    assertFalse(
        "Process should have been removed from active process list", 
        ctx.getServices().getProcesses().getActiveProcesses().containsProcess(proc.getProcessID())
    );
    
  }
  
  @Test
  public void testStaleVmCheckNoAutoRestart() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    
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
        ctx.getServices().getProcesses().getActiveProcesses().containsProcess(proc.getProcessID())
    );
    
    verify(dispatcher).dispatch(any(ProcessStaleEvent.class));
    verify(os, never()).killProcess(any(LogCallback.class), anyString());
    
  }  
  
}
