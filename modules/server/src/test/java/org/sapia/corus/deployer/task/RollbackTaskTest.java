package org.sapia.corus.deployer.task;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.TestServerContext;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.event.RollbackCompletedEvent;
import org.sapia.corus.client.services.deployer.event.RollbackFailedEvent;
import org.sapia.corus.client.services.deployer.event.RollbackStartingEvent;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.taskmanager.core.TaskParams;

@RunWith(MockitoJUnitRunner.class)
public class RollbackTaskTest {
  
  @Mock
  private Processor processor;
  
  @Mock
  private FileSystemModule fs;
  
  @Mock
  private Deployer deployer;
  
  @Mock
  private EventDispatcher dispatcher;
  
  @Mock
  private File scriptBaseDirDir, scriptFile;
    
  private TestServerContext ctx;
  
  private RollbackTask task;
  
  private Distribution dist;
  
  @Before
  public void setUp() throws Exception {
    
    ctx = TestServerContext.create();
    task = new RollbackTask();
    
    dist = new Distribution("test", "1.0");
    dist.setBaseDir("baseDir");
    
    ctx.getServices().rebind(FileSystemModule.class, fs);
    ctx.getServices().rebind(Processor.class, processor);
    ctx.getServices().rebind(Deployer.class, deployer);
    ctx.getServices().rebind(EventDispatcher.class, dispatcher);
    
    doAnswer(new Answer<File>() {
      @Override
      public File answer(InvocationOnMock invocation) throws Throwable {
        String fName = (String) invocation.getArguments()[0];
        if (fName.contains("rollback.corus")) return scriptFile;
        if (fName.contains("common")) return scriptBaseDirDir;
        else {
          File mockFile = mock(File.class);
          when(mockFile.getAbsolutePath()).thenReturn("test");
          when(mockFile.exists()).thenReturn(true);
          return mockFile;
        }
      }
    }).when(fs).getFileHandle(anyString());
    
    when(fs.getFileReader(any(File.class))).thenReturn(new StringReader("echo \"test\""));
    when(scriptBaseDirDir.exists()).thenReturn(true);
    when(scriptFile.exists()).thenReturn(true);
    
  }
  
  @Test
  public void testExecute() throws Exception {
    ctx.getTm().executeAndWait(task, TaskParams.createFor(dist)).get();
    
    verify(deployer).getDistributions(any(DistributionCriteria.class));
    verify(dispatcher).dispatch(isA(RollbackStartingEvent.class));
    verify(dispatcher).dispatch(isA(RollbackCompletedEvent.class));

  }
  
  @Test(expected = InvocationTargetException.class)
  public void testExecute_script_not_found() throws Exception {
    when(scriptFile.exists()).thenReturn(false);

    ctx.getTm().executeAndWait(task, TaskParams.createFor(dist)).get();
  }
  
  @Test
  public void testExecute_script_io_error() throws Exception {
    when(fs.getFileReader(any(File.class))).thenThrow(new IOException("I/O error"));
    
    try {
      ctx.getTm().executeAndWait(task, TaskParams.createFor(dist)).get();
    } catch (Throwable e) {
      // noop
    }
    
    verify(dispatcher).dispatch(isA(RollbackStartingEvent.class));
    verify(dispatcher).dispatch(isA(RollbackFailedEvent.class));
  }
  
  
  @Test(expected = InvocationTargetException.class)
  public void testExecute_processes_still_running() throws Exception {
    List<Distribution> dists = mock(List.class);
    List<Process> processes = mock(List.class);
    
    when(dists.size()).thenReturn(10);
    when(dists.isEmpty()).thenReturn(false);
    when(processes.size()).thenReturn(10);
    when(processes.isEmpty()).thenReturn(false);
    
    when(deployer.getDistributions(any(DistributionCriteria.class))).thenReturn(dists);
    when(processor.getProcesses(any(ProcessCriteria.class))).thenReturn(processes);
    
    ctx.getTm().executeAndWait(task, TaskParams.createFor(dist)).get();
  }
  
}
