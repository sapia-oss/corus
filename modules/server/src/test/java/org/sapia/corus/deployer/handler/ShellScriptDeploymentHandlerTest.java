package org.sapia.corus.deployer.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.ShellScriptDeploymentMetadata;
import org.sapia.corus.deployer.InternalShellScriptManager;

@RunWith(MockitoJUnitRunner.class)
public class ShellScriptDeploymentHandlerTest {

  private ScriptDeploymentHandler handler;
  
  @Mock
  private DeployerConfiguration config;
  
  @Mock
  private InternalShellScriptManager manager;
  
  @Mock
  private ProgressQueue progress;
  
  @Before
  public void setUp() {
    handler = new ScriptDeploymentHandler();
    handler.setConfiguration(config);
    handler.setManager(manager);

    when(config.getScriptDir()).thenReturn("scriptDir");
    when(manager.addScript(any(ShellScript.class), any(File.class))).thenReturn(progress);
  }

  @Test
  public void testAccepts() {
    DeploymentMetadata meta = new ShellScriptDeploymentMetadata("test", 100, "alias", "desc", DeployPreferences.newInstance(), new ClusterInfo(true));
    assertTrue(handler.accepts(meta));
  }

  @Test
  public void testGetDestFile() {
    DeploymentMetadata meta = new ShellScriptDeploymentMetadata("test", 100, "alias", "desc", DeployPreferences.newInstance(), new ClusterInfo(true));
    File destFile = handler.getDestFile(meta);
    assertTrue(destFile.getAbsolutePath().contains("scriptDir" + File.separator + "test"));
  }

  @Test
  public void testCompleteDeployment() {
    DeploymentMetadata meta = new ShellScriptDeploymentMetadata("test", 100, "alias", "desc", DeployPreferences.newInstance(), new ClusterInfo(true));
    ProgressQueue q = handler.completeDeployment(meta, new File("test"));
    assertNotNull(q);
  }

}

