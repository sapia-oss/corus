package org.sapia.corus.deployer.task;


import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.processor.task.TestBaseTask;
import org.sapia.corus.taskmanager.core.TaskParams;

public class UndeployTaskTest extends TestBaseTask{
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    super.createDistribution("test", "1.0");
  }
  
  @Test
  public void testExecute() throws Exception{
    FileSystemModule fs = mock(FileSystemModule.class);
    ctx.getServices().rebind(FileSystemModule.class, fs);
    
    UndeployTask task = new UndeployTask();
    ctx.getTm().executeAndWait(
        task, 
        TaskParams.createFor(DistributionCriteria.builder().name(ArgFactory.exact("test")).version(ArgFactory.exact("1.0")).build())
    ).get();
    
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();
    
    assertFalse("Distribution was not removed", ctx.getServices().getDistributions().containsDistribution(criteria));
    
  }  

}
