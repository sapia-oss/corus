package org.sapia.corus.processor.task;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.exceptions.port.PortUnavailableException;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.taskmanager.core.TaskParams;

/**
 * @author Yanick Duchesne
 */
public class RestartTaskTest extends TestBaseTask{

  private Process  proc, procWithPort;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    Distribution  dist  = super.createDistribution("testDist", "1.0");
    ProcessConfig conf  = super.createProcessConfig(dist, "testProc", "testProfile");
    proc = super.createProcess(dist, conf, "testProfile");
    proc.setMaxKillRetry(1);
    proc.save();
    
    ProcessConfig confWithPort  = super.createProcessConfig(dist, "testProcWithPort", "testProfile");
    confWithPort.createPort().setName("testPortRange");
    procWithPort = super.createProcess(dist, confWithPort, "testProfile");
    procWithPort.setMaxKillRetry(1);
    procWithPort.save();
  }
  
  @Test
  public void testExecute() throws Exception{
    OsModule os = mock(OsModule.class);
    ctx.getServices().rebind(OsModule.class, os);
    
    proc.confirmKilled();
    proc.save();
    long oldCreationTime = proc.getCreationTime();
    long lastAccessTime  = proc.getLastAccess();
    Thread.sleep(100);
    RestartTask restart = new RestartTask(proc.getMaxKillRetry());
    TaskParams<Process, ProcessTerminationRequestor, Void, Void> params = 
      TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_PROCESS);
    tm.executeAndWait(restart, params).get();
    proc.getLock().awaitRelease(10, TimeUnit.SECONDS);
    
    assertNotSame("Creation times should not be identical", oldCreationTime, proc.getCreationTime());
    assertNotSame("Last access times should not be identical", lastAccessTime, proc.getLastAccess());    
    assertTrue("Process should be active", ctx.getServices().getProcesses().getActiveProcesses().containsProcess(proc.getProcessID()));
    verify(os).killProcess(any(OsModule.LogCallback.class), anyString());    
  }
  
  @Test
  public void testRestartFailed() throws Exception{
    OsModule os = mock(OsModule.class);
    ctx.getServices().rebind(OsModule.class, os);
    
    PortManager pm = mock(PortManager.class);
    ctx.getServices().rebind(PortManager.class, pm);
    when(pm.aquirePort(anyString())).thenThrow(new PortUnavailableException("port unavailable"));
    
    procWithPort.confirmKilled();
    procWithPort.save();
    Thread.sleep(100);
    RestartTask restart = new RestartTask(proc.getMaxKillRetry());
    TaskParams<Process, ProcessTerminationRequestor, Void, Void> params = 
      TaskParams.createFor(procWithPort, ProcessTerminationRequestor.KILL_REQUESTOR_PROCESS);
    tm.executeAndWait(restart, params).get();
    procWithPort.getLock().awaitRelease(10, TimeUnit.SECONDS);
    
    assertFalse(
        "Process should have been removed from restart list", 
        ctx.getServices().getProcesses().getProcessesToRestart().containsProcess(procWithPort.getProcessID())
    );
    verify(os).killProcess(any(OsModule.LogCallback.class), anyString());    
  }

}
