package org.sapia.corus.deployer.task;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.event.UndeploymentCompletedEvent;
import org.sapia.corus.client.services.deployer.event.UndeploymentFailedEvent;
import org.sapia.corus.client.services.deployer.event.UndeploymentStartingEvent;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.processor.task.TestBaseTask;
import org.sapia.corus.taskmanager.core.TaskParams;

@RunWith(MockitoJUnitRunner.class)
public class UndeployTaskTest extends TestBaseTask {
  
  @Mock
  private EventDispatcher  dispatcher;
  
  @Mock
  private FileSystemModule fs;

  private UndeployTask     task;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    super.createDistribution("test", "1.0");
  
    task = new UndeployTask();
    
    ctx.getServices().rebind(FileSystemModule.class, fs);
    ctx.getServices().rebind(EventDispatcher.class, dispatcher);
  }
  
  @Test
  public void testExecute() throws Exception {
    ctx.getTm().executeAndWait(
        task, 
        TaskParams.createFor(DistributionCriteria.builder().name(ArgMatchers.exact("test")).version(ArgMatchers.exact("1.0")).build())
    ).get();
    
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();
    
    assertFalse("Distribution was not removed", ctx.getServices().getDistributions().containsDistribution(criteria));
    verify(dispatcher).dispatch(isA(UndeploymentStartingEvent.class));
    verify(dispatcher).dispatch(isA(UndeploymentCompletedEvent.class));
  }  

  @Test
  public void testExecute_failure() throws Exception {
    doThrow(new IOException("Could not create file")).when(fs).deleteDirectory(any(File.class));
    
    try {
      ctx.getTm().executeAndWait(
          task, 
          TaskParams.createFor(DistributionCriteria.builder().name(ArgMatchers.exact("test")).version(ArgMatchers.exact("1.0")).build())
      ).get();
    } catch (Throwable t) {
      // noop 
    }
    
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();

    assertTrue("Distribution was not removed", ctx.getServices().getDistributions().containsDistribution(criteria));
    verify(dispatcher).dispatch(isA(UndeploymentStartingEvent.class));
    verify(dispatcher).dispatch(isA(UndeploymentFailedEvent.class));
  }  
}
