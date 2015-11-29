package org.sapia.corus.deployer.processor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.deployer.processor.DeploymentContext;
import org.sapia.corus.deployer.processor.DeploymentPostProcessor;
import org.sapia.corus.deployer.processor.DeploymentProcessorManagerImpl;
import org.sapia.corus.deployer.processor.UndeploymentPostProcessor;
import org.sapia.ubik.util.Collects;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentProcessorManagerImplTest {

  @Mock
  private ApplicationContext appContext;
  
  @Mock
  private DeploymentPostProcessor d1, d2;
  
  @Mock
  private UndeploymentPostProcessor u1, u2;
  
  @Mock
  private Map<String, DeploymentPostProcessor> deploymentProcessors;
  
  @Mock
  private Map<String, UndeploymentPostProcessor> undeploymentProcessor;
  
  private DeploymentProcessorManagerImpl manager;
  
  @Before
  public void setUp() throws Exception {
    manager = new DeploymentProcessorManagerImpl();
    manager.setApplicationContext(appContext);
    
    when(deploymentProcessors.values()).thenReturn(Collects.arrayToList(d1, d2));
    when(undeploymentProcessor.values()).thenReturn(Collects.arrayToList(u1, u2));
    
    when(appContext.getBeansOfType(DeploymentPostProcessor.class)).thenReturn(deploymentProcessors);
    when(appContext.getBeansOfType(UndeploymentPostProcessor.class)).thenReturn(undeploymentProcessor);
    
    manager.init();
  }

  @Test
  public void testOnPostDeploy() throws Exception {
    manager.onPostDeploy(new DeploymentContext(new Distribution("test", "1.0")), mock(LogCallback.class));
    
    verify(d1).onPostDeploy(any(DeploymentContext.class), any(LogCallback.class));
    verify(d2).onPostDeploy(any(DeploymentContext.class), any(LogCallback.class));
  }

  @Test
  public void testOnPostUndeploy() throws Exception {
    manager.onPostUndeploy(new DeploymentContext(new Distribution("test", "1.0")), mock(LogCallback.class));
    
    verify(u1).onPostUndeploy(any(DeploymentContext.class), any(LogCallback.class));
    verify(u2).onPostUndeploy(any(DeploymentContext.class), any(LogCallback.class));
  }

}
