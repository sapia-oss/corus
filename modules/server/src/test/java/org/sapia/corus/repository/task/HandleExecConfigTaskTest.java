package org.sapia.corus.repository.task;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.client.services.processor.ProcessDef;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.core.InternalServiceContext;
import org.sapia.ubik.util.Collects;

public class HandleExecConfigTaskTest extends AbstractRepoTaskTest {

  private HandleExecConfigTask configsTask, emptyConfigsTask, startOnBootConfigTask;
  private Deployer             deployer;
  private Processor            processor;
  
  @Before
  public void setUp() {
    super.doSetUp();
    deployer  = mock(Deployer.class);
    processor = mock(Processor.class);
    
    ExecConfig conf1 = new ExecConfig();
    ProcessDef proc1 = conf1.createProcess();
    proc1.setName("proc1");
    proc1.setDist("dist1");
    proc1.setVersion("v1");
    
    ExecConfig conf2 = new ExecConfig();
    ProcessDef proc2 = conf1.createProcess();
    proc2.setName("proc2");
    proc2.setDist("dist2");
    proc2.setVersion("v2");
    
    ExecConfig conf3 = new ExecConfig();
    conf3.setStartOnBoot(true);
    ProcessDef proc3 = conf3.createProcess();
    proc3.setName("proc3");
    proc3.setDist("dist3");
    proc3.setVersion("v3");
    
    configsTask           = new HandleExecConfigTask(repoConfig, Collects.arrayToList(conf1, conf2));
    emptyConfigsTask      = new HandleExecConfigTask(repoConfig, new ArrayList<ExecConfig>());
    startOnBootConfigTask = new HandleExecConfigTask(repoConfig, Collects.arrayToList(conf3));
    
    InternalServiceContext services = mock(InternalServiceContext.class);
    when(serverContext.getServices()).thenReturn(services);
    when(services.getDeployer()).thenReturn(deployer);
    when(services.getProcessor()).thenReturn(processor);
  }
  
  @Test
  public void testExecuteWithoutConfigs() throws Throwable {
    emptyConfigsTask.execute(taskContext, null);
    verify(deployer, never()).getDistribution(any(DistributionCriteria.class));
  }
 
  @Test
  public void testExecuteWithConfigs() throws Throwable {
    configsTask.execute(taskContext, null);
    verify(deployer, times(2)).getDistribution(any(DistributionCriteria.class));
    verify(processor, times(2)).addExecConfig(any(ExecConfig.class));
    verify(processor, never()).execConfig(any(ExecConfigCriteria.class));
  }

  @Test
  public void testExecuteWithStartOnBootConfigs() throws Throwable {
    startOnBootConfigTask.execute(taskContext, null);
    verify(deployer, times(1)).getDistribution(any(DistributionCriteria.class));
    verify(processor, times(1)).addExecConfig(any(ExecConfig.class));
    verify(processor, times(1)).execConfig(any(ExecConfigCriteria.class));
  }
  
  @Test
  public void testExecuteWithStartOnBootConfigsDisabled() throws Throwable {
    repoConfig.setBootExecEnabled(false);
    startOnBootConfigTask.execute(taskContext, null);
    verify(deployer, times(1)).getDistribution(any(DistributionCriteria.class));
    verify(processor, times(1)).addExecConfig(any(ExecConfig.class));
    verify(processor, never()).execConfig(any(ExecConfigCriteria.class));
  }  

}
