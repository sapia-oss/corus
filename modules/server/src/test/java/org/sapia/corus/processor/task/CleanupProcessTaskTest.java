package org.sapia.corus.processor.task;

import static org.junit.Assert.assertFalse;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;

public class CleanupProcessTaskTest extends TestBaseTask{

  private Process          proc;
  private FileSystemModule fs;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    Distribution  dist = super.createDistribution("testDist", "1.0");
    ProcessConfig conf = super.createProcessConfig(dist, "testProc", "testProfile");
    proc = super.createProcess(dist, conf, "testProfile");
    ctx.getServices().rebind(FileSystemModule.class, fs = mock(FileSystemModule.class));
  }
  
  @Test
  public void testExecute() throws Exception{
    CleanupProcessTask task = new CleanupProcessTask();
    proc.setStatus(LifeCycleStatus.KILL_CONFIRMED);
    proc.setDeleteOnKill(true);
    proc.save();
    ctx.getTm().executeAndWait(task, proc).get();
    verify(fs).deleteDirectory(any(File.class));
    assertFalse(
        "Process should have been removed from active process list", 
        ctx.getProc().getProcessDB().containsProcess(proc.getProcessID())
    );
  }
  
  @Test
  public void testExecuteNotDeleteOnKill() throws Exception{
    CleanupProcessTask task = new CleanupProcessTask();
    proc.setStatus(LifeCycleStatus.KILL_CONFIRMED);
    proc.setDeleteOnKill(false);
    proc.save();
    ctx.getTm().executeAndWait(task, proc).get();
    verify(fs, times(0)).deleteDirectory(any(File.class));
    assertFalse(
        "Process should have been removed from active process list", 
        ctx.getProc().getProcessDB().containsProcess(proc.getProcessID())
    );
  }
  
  @Test(expected=InvocationTargetException.class)
  public void testExecuteNotKillConfirmed() throws Exception{
    CleanupProcessTask task = new CleanupProcessTask();
    proc.setStatus(LifeCycleStatus.ACTIVE);
    proc.setDeleteOnKill(true);
    proc.save();
    ctx.getTm().executeAndWait(task, proc).get();
  }
}
