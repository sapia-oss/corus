package org.sapia.corus.deployer.handler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.transport.DockerImageDeploymentMetadata;
import org.sapia.corus.docker.DockerClientFacade;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.corus.taskmanager.core.SequentialTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskManager;

@RunWith(MockitoJUnitRunner.class)
public class DockerImageDeploymentHandlerTest {
  
  @Mock
  private DeployerConfiguration config;
  
  @Mock
  private DockerFacade dockerFacade;
  
  @Mock
  private TaskManager taskMan;
  
  @Mock
  private TaskExecutionContext taskContext;
  
  @Mock
  private DockerClientFacade dockerClient;

  private File tempDir;
  
  private File imageFile;
  
  private DockerImageDeploymentMetadata metadata;

  private DockerImageDeploymentHandler handler;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    
    tempDir = FilePath.forJvmTempDir().addDir(UUID.randomUUID().toString()).createFile();
    tempDir.mkdirs();
    imageFile = new File(tempDir, "test-image_1.0.tar");
    imageFile.createNewFile(); 
    metadata = new DockerImageDeploymentMetadata(
        "test-image:1.0", "test-image_1.0.tar", 1000,
        DeployPreferences.newInstance(), ClusterInfo.clustered()
    );
 
    handler = new DockerImageDeploymentHandler();
    handler.setConfiguration(config);
    handler.setDockerFacade(dockerFacade);
    handler.setTaskManager(taskMan);
 
    when(dockerFacade.getDockerClient()).thenReturn(dockerClient);
    when(config.getTempDir()).thenReturn(tempDir.getAbsolutePath());
    
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Task<Void, Void> t = (Task<Void, Void>) invocation.getArgumentAt(0, Task.class);
        t.execute(taskContext, null);
        SequentialTaskConfig config = invocation.getArgumentAt(2, SequentialTaskConfig.class);
        config.getLog().close();
        return null;
      }
    }).when(taskMan).execute(any(Task.class), any(Object.class), any(SequentialTaskConfig.class));
  }

  @Test
  public void testAccepts() {
    assertTrue(handler.accepts(metadata));
  }

  @Test
  public void testGetDestFile() {
    File f = handler.getDestFile(metadata);
    
    assertTrue(f.getAbsolutePath().contains("test-image_1.0.tar"));
  }

  @Test
  public void testCompleteDeployment() throws IOException {
    ProgressQueue progress = handler.completeDeployment(metadata, imageFile);
    verify(dockerClient).loadImage(anyString(), any(InputStream.class), any(LogCallback.class));
    progress.fetchNext();
    assertFalse(progress.hasNext());
  }
  
  @Test
  public void testCompleteDeployment_image_already_loaded() throws IOException {
    when(dockerClient.containsImage(anyString())).thenReturn(true);
    
    ProgressQueue progress = handler.completeDeployment(metadata, imageFile);
    verify(dockerClient, never()).loadImage(anyString(), any(InputStream.class), any(LogCallback.class));
    progress.fetchNext();
    assertFalse(progress.hasNext());
  }


}
