package org.sapia.corus.deployer.handler;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.FileDeploymentMetadata;

@RunWith(MockitoJUnitRunner.class)
public class FileDeploymentHandlerTest {

  private FileDeploymentHandler handler;
  
  @Mock
  private DeployerConfiguration config;
  
  @Before
  public void setUp() {
    handler = new FileDeploymentHandler();
    handler.setConfiguration(config);

    when(config.getUploadDir()).thenReturn("uploadDir");
  }

  @Test
  public void testAccepts() {
    DeploymentMetadata meta = new FileDeploymentMetadata("test", 100, "destDir", DeployPreferences.newInstance(), new ClusterInfo(true));
    assertTrue(handler.accepts(meta));
  }

  @Test
  public void testGetDestFileWithDestDir() {
    DeploymentMetadata meta = new FileDeploymentMetadata("test", 100, "destDir", DeployPreferences.newInstance(), new ClusterInfo(true));
    File destFile = handler.getDestFile(meta);
    assertTrue(destFile.getAbsolutePath().contains("destDir" + File.separator + "test"));
  }
  
  @Test
  public void testGetDestFileWithoutDestDir() {
    DeploymentMetadata meta = new FileDeploymentMetadata("test", 100, null, DeployPreferences.newInstance(), new ClusterInfo(true));
    File destFile = handler.getDestFile(meta);
    assertTrue(destFile.getAbsolutePath().contains("uploadDir" + File.separator + "test"));
  }  

  @Test
  public void testCompleteDeployment() {
    DeploymentMetadata meta = new FileDeploymentMetadata("test", 100, "destDir", DeployPreferences.newInstance(), new ClusterInfo(true));
    ProgressQueue q = handler.completeDeployment(meta, new File("test"));
    assertNotNull(q);
  }

}
