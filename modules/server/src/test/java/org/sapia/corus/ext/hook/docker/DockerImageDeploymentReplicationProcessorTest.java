package org.sapia.corus.ext.hook.docker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.IDGenerator;
import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter;
import org.sapia.corus.deployer.processor.DeploymentContext;
import org.sapia.corus.docker.DockerClientFacade;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.corus.taskmanager.core.FutureResult;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.ubik.net.ServerAddress;

@RunWith(MockitoJUnitRunner.class)
public class DockerImageDeploymentReplicationProcessorTest {
  
  @Mock
  private DeployerConfiguration configuration;
  
  @Mock
  private DockerFacade dockerFacade;
  
  @Mock
  private DockerClientFacade dockerClient;
  
  @Mock
  private TaskExecutionContext taskContext;
  
  @Mock
  private TaskManager taskManager;
  
  private DockerImageDeploymentReplicationProcessor processor;
  
  private Distribution distribution;
  
  private File repoDir;

  @Before
  public void setUp() throws Exception {
    repoDir = FilePath.forJvmTempDir().addDir("test-" + IDGenerator.makeBase62Id(5)).createFile();
    repoDir.mkdirs();
    assertTrue(repoDir.exists());
    
    distribution = new Distribution("test", "1.0");
    ProcessConfig pc = new ProcessConfig("test-process");
    DockerStarter st = new DockerStarter();
    st.setImage("test-image");
    pc.addStarter(st);
    distribution.addProcess(pc);
    
    processor = new DockerImageDeploymentReplicationProcessor();
    processor.setConfiguration(configuration);
    processor.setDockerFacade(dockerFacade);
    
    when(taskContext.getTaskManager()).thenReturn(taskManager);
    when(configuration.getRepoDir()).thenReturn(repoDir.getAbsolutePath());
    when(dockerFacade.getDockerClient()).thenReturn(dockerClient);
    doAnswer(new Answer<InputStream>() {
      @Override
      public InputStream answer(InvocationOnMock invocation) throws Throwable {
        return new ByteArrayInputStream("TEST".getBytes());
      }
    }).when(dockerClient).saveImage(anyString(), any(LogCallback.class));
    
    doAnswer(new Answer<FutureResult<Void>>() {
      @Override
      public FutureResult<Void> answer(InvocationOnMock invocation) throws Throwable {
        Task t = invocation.getArgumentAt(0, Task.class);
        t.execute(taskContext, null);
        return mock(FutureResult.class);
      }
    }).when(taskManager).executeAndWait(any(Task.class), anyObject());
    
  }
  
  @Test
  public void testAccepts() {
    assertTrue(processor.accepts(new DeploymentContext(distribution)));
  }
  
  @Test
  public void testAccepts_no_docker_starter() {
    distribution = new Distribution("test", "1.0");
    assertFalse(processor.accepts(new DeploymentContext(distribution)));
  }

  @Test
  public void testGetImageDeploymentTaskFor_image_name_on_starter() throws Throwable {  
    Task<Void, Void> task = processor.getImageDeploymentTaskFor(new DeploymentContext(distribution), getEndpoints());
    task.execute(taskContext, null);
    File imageFile = FilePath.newInstance().addDir(repoDir.getAbsolutePath()).setRelativeFile("docker_test-image.tar").createFile();
    assertEquals("TEST", IOUtil.textStreamToString(new FileInputStream(imageFile)).trim());
  }
  
  @Test
  public void testGetImageDeploymentTaskFor_no_image_name_on_starter() throws Throwable {  
    distribution = new Distribution("test", "1.0");
    ProcessConfig pc = new ProcessConfig("test-process");
    DockerStarter st = new DockerStarter();
    pc.addStarter(st);
    distribution.addProcess(pc);
    
    Task<Void, Void> task = processor.getImageDeploymentTaskFor(new DeploymentContext(distribution), getEndpoints());

    task.execute(taskContext, null);
    File imageFile = FilePath.newInstance().addDir(repoDir.getAbsolutePath()).setRelativeFile("docker_test_1.0.tar").createFile();
    assertEquals("TEST", IOUtil.textStreamToString(new FileInputStream(imageFile)).trim());
  }
  
  @Test(expected = IllegalStateException.class)
  public void testGetImageDeploymentTaskFor_docker_error() throws Throwable {  
    doAnswer(new Answer<InputStream>() {
      @Override
      public InputStream answer(InvocationOnMock invocation) throws Throwable {
        LogCallback callback = invocation.getArgumentAt(1, LogCallback.class);
        callback.error("Docker error");
        return null;
      }
    }).when(dockerClient).saveImage(anyString(), any(LogCallback.class));
    
    
    Task<Void, Void> task = processor.getImageDeploymentTaskFor(new DeploymentContext(distribution), getEndpoints());
    task.execute(taskContext, null);
  }
  
  
  private List<Endpoint> getEndpoints() {
    List<Endpoint> endpoints = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      Endpoint ep = new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class));
      endpoints.add(ep);
    }
    return endpoints;
  }

}
