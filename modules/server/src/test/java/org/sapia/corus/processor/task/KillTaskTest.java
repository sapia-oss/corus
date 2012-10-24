package org.sapia.corus.processor.task;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.exceptions.processor.ProcessLockException;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.Port;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.taskmanager.core.TaskParams;

/**
 * @author Yanick Duchesne
 */
public class KillTaskTest extends TestBaseTask{
  
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
  public void testKillFromCorusNotConfirmed() throws Exception {
    KillTask kill = new KillTask(3);  
    tm.executeAndWait(kill, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)).get();
    assertTrue(
        "Process should not have been killed", 
        ctx.getProc().getProcessDB().getActiveProcesses().containsProcess(proc.getProcessID())
    );
  }

  @Test
  public void testKillFromCorusConfirmed() throws Exception {
    OsModule os = mock(OsModule.class);
    ctx.getServices().rebind(OsModule.class, os);

    proc.confirmKilled();
    proc.save();
    KillTask kill = new KillTask(3);  
    tm.executeAndWait(kill, TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN)).get();
    
    assertFalse(
        "Process should have been killed", 
        ctx.getProc().getProcessDB().getActiveProcesses().containsProcess(proc.getProcessID())
    );
    assertEquals("Port should have been released", 1, ctx.getPorts().getPortRanges().get(0).getAvailable().size());
    
    verify(os).killProcess(any(OsModule.LogCallback.class), anyString());
  }

  @Test
  public void testKillMaxAttemptReachedNoRestart() throws Exception {
    OsModule os = mock(OsModule.class);
    ctx.getServices().rebind(OsModule.class, os);
    
    int      maxAttempts = 3;
    KillTask kill        = new KillTask(maxAttempts);
    for(int i = 0; i < maxAttempts; i++){
      tm.executeAndWait(
          kill, 
          TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN)
      ).get();
    }
    
    assertTrue(
        "Process should not have been killed", 
        ctx.getProc().getProcessDB().getActiveProcesses().containsProcess(proc.getProcessID())
    );

    // ultimate attempt
    tm.executeAndWait(
        kill, 
        TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN)
    ).get();
    
    assertFalse(
        "Process should have been killed and not restarted", 
        ctx.getProc().getProcessDB().getActiveProcesses().containsProcess(proc.getProcessID())
    );
    verify(os).killProcess(any(OsModule.LogCallback.class), anyString());    
  }

  @Test
  public void testKillMaxAttemptReachedWithRestart() throws Exception {
    int      maxAttempts = 3;
    String   oldPid = proc.getOsPid();
    ctx.getProc().getConfigurationImpl().setRestartInterval(0);
    KillTask kill        = new KillTask(maxAttempts);
    for(int i = 0; i < maxAttempts; i++){
      tm.executeAndWait(
          kill, 
          TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)
      ).get();
    }
    
    assertTrue(
        "Process should not have been killed", 
        ctx.getProc().getProcessDB().getActiveProcesses().containsProcess(proc.getProcessID())
    );

    // ultimate attempt
    tm.executeAndWait(
        kill, 
        TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)
    ).get();
    
    assertTrue(
        "Process should have been killed and restarted", 
        ctx.getProc().getProcessDB().getActiveProcesses().containsProcess(proc.getProcessID())
    );
    
    Process restarted = ctx.getProc().getProcessDB().getActiveProcesses().getProcess(proc.getProcessID());
    assertNotSame("Restarted process should have new PID", oldPid, restarted.getOsPid());
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
        ctx.getProc().getProcessDB().getActiveProcesses().containsProcess(proc.getProcessID())
    );
  }
  
  
  @Test(expected=ProcessLockException.class)
  public void testConcurrentAccess() throws Exception{
    KillTask kill = new KillTask(3);
    tm.executeAndWait(
          kill, 
          TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER)
    ).get();
    
    proc.getLock().acquire(LockOwner.createInstance());
  }

}
