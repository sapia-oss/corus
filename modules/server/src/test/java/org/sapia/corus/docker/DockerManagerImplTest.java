package org.sapia.corus.docker;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.common.range.IntRange;
import org.sapia.corus.client.services.docker.DockerImage;
import org.sapia.corus.taskmanager.core.SequentialTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.ubik.util.Func;

@RunWith(MockitoJUnitRunner.class)
public class DockerManagerImplTest {

  @Mock
  private TaskManager tasks;
  
  @Mock
  private DockerFacade dockerFacade;
  
  @Mock
  private DockerClientFacade dockerClient;
  
  @Mock
  private TaskExecutionContext taskContext;
  
  private DockerManagerImpl manager;
  
  private List<DockerImage> images;
  
  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    manager = new DockerManagerImpl();
    manager.setTaskMan(tasks);
    manager.setDockerFacade(dockerFacade);
    
    when(dockerFacade.getDockerClient()).thenReturn(dockerClient);
    
    images = IntRange.forLength(5).asList(new Func<DockerImage, Integer>() {
      @Override
      public DockerImage call(Integer index) {
        DockerImage img = new DockerImage("img-" + index, "" + System.currentTimeMillis());
        img.getTags().add("test:1.0");
        return img;
      }
    });
    
    when(dockerClient.listImages(ArgMatchers.any())).thenReturn(images);
    
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Task<Void, Void> task = invocation.getArgumentAt(0, Task.class);
        task.execute(taskContext, null);
        SequentialTaskConfig conf = invocation.getArgumentAt(2, SequentialTaskConfig.class);
        conf.getLog().close();
        return null;
      }
    }).when(tasks).execute(any(Task.class), any(Object.class), any(SequentialTaskConfig.class));
  }

  @Test
  public void testGetImages() throws Exception {
    manager.getImages(ArgMatchers.any());
    
    verify(dockerClient).listImages(any(ArgMatcher.class));
  }

  @Test
  public void testGetContainers() throws Exception {
    manager.getContainers(ArgMatchers.any());
    
    verify(dockerClient).listContainers(any(ArgMatcher.class));
  }

  @Test
  public void testRemoveImages() {
    manager.removeImages(ArgMatchers.any());
    
    verify(dockerClient, times(5)).removeImage(anyString(), any(LogCallback.class));
  }

  @Test
  public void testPullImage() {
    manager.pullImage("test:1.0");
    
    verify(dockerClient).pullImage(eq("test:1.0"), any(LogCallback.class));
  }

  @Test
  public void testPullImage_image_already_loaded() {
    when(dockerClient.containsImage(anyString())).thenReturn(true);
    
    manager.pullImage("test:1.0");
    
    verify(dockerClient, never()).pullImage(eq("test:1.0"), any(LogCallback.class));
  }
}
