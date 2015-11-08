package org.sapia.corus.deployer.task;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.FileFacade;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.core.InternalServiceContext;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.SysClock.MutableClock;

@RunWith(MockitoJUnitRunner.class)
public class CleanTempDirTaskTest {
  
  @Mock
  private TaskExecutionContext   taskContext;
  @Mock
  private ServerContext          serverContext;
  private InternalServiceContext serviceContext;
  @Mock
  private FileSystemModule fs;
  @Mock
  private FileFacade toDeleteFacade, toKeepFacade;
  @Mock
  private DeployerConfiguration conf;
  @Mock
  private Deployer deployer;
  
  private File tmpDir;
  private File toDelete, toKeep;
  private MutableClock clock;
  private CleanTempDirTask task;

  @Before
  public void setUp() throws Exception {
    tmpDir   = new File("testTmpDir");
    toDelete = new File("toDelete");
    toKeep   = new File("toKeep");
    
    clock = new MutableClock();
    task  = new CleanTempDirTask();
    task.setClock(clock);
    clock.increaseCurrentTimeMillis(TimeUnit.HOURS.toMillis(1));
    
    serviceContext = new InternalServiceContext();
    serviceContext.rebind(FileSystemModule.class, fs);
    serviceContext.rebind(Deployer.class, deployer);
    
    when(deployer.getConfiguration()).thenReturn(conf);
    when(conf.getTempFileTimeoutHours()).thenReturn(1);
    when(taskContext.getServerContext()).thenReturn(serverContext);
    when(serverContext.getServices()).thenReturn(serviceContext);
   
    when(fs.getFileHandle(anyString())).thenReturn(tmpDir);
    when(fs.listFiles(tmpDir)).thenReturn(Collects.arrayToList(toDelete, toKeep));
    
    when(fs.getFileFacade(toDelete)).thenReturn(toDeleteFacade);
    when(fs.getFileFacade(toKeep)).thenReturn(toKeepFacade);
    
    when(toDeleteFacade.lastModified()).thenReturn(0L);
    when(toDeleteFacade.isFile()).thenReturn(true);
    when(toKeepFacade.lastModified()).thenReturn(clock.currentTimeMillis());
    when(toKeepFacade.isFile()).thenReturn(true);
  }

  @Test
  public void testExecute() throws Throwable {
    task.execute(taskContext, null);
    verify(fs).deleteFile(toDelete);
    verify(fs, never()).deleteFile(toKeep);
  }

}
