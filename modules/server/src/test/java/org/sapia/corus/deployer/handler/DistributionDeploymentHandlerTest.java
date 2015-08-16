package org.sapia.corus.deployer.handler;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.reference.DefaultReference;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.services.ModuleState;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DistributionDeploymentMetadata;
import org.sapia.corus.taskmanager.core.SequentialTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.taskmanager.core.TaskParams;

@RunWith(MockitoJUnitRunner.class)
public class DistributionDeploymentHandlerTest {
  
  private DistributionDeploymentHandler handler;
  
  @Mock
  private TaskManager taskManager;
  
  @Mock
  private DeployerConfiguration config;
  
  @Mock
  private Deployer deployer;
  
  private Reference<ModuleState> state;
  
  @Before
  public void setUp() {
    state = DefaultReference.of(ModuleState.IDLE);
    handler = new DistributionDeploymentHandler();
    handler.setDeployer(deployer);
    handler.setTaskman(taskManager);

    when(config.getTempDir()).thenReturn("tempDir");
    when(deployer.getState()).thenReturn(state);
    when(deployer.getConfiguration()).thenReturn(config);
  }

  @Test
  public void testAccepts() {
    DeploymentMetadata meta = new DistributionDeploymentMetadata("test", 100, DeployPreferences.newInstance(), new ClusterInfo(true));
    assertTrue(handler.accepts(meta));
  }

  @Test
  public void testGetDestFile() {
    DeploymentMetadata meta = new DistributionDeploymentMetadata("test", 100, DeployPreferences.newInstance(), new ClusterInfo(true));
    File destFile = handler.getDestFile(meta);
    assertTrue(destFile.getAbsolutePath().contains("tempDir" + File.separator + "test"));
  }

  @Test
  public void testCompleteDeployment() {
    DeploymentMetadata meta = new DistributionDeploymentMetadata("test", 100, DeployPreferences.newInstance(), new ClusterInfo(true));
    handler.completeDeployment(meta, new File("test"));
    verify(taskManager).execute(any(Task.class), any(TaskParams.class), any(SequentialTaskConfig.class));
  }

}