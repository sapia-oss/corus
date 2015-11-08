package org.sapia.corus.ext.hook.docker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.LogCallback;
import org.sapia.corus.client.services.deployer.dist.Property;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.deployer.dist.StarterType;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.corus.processor.hook.ProcessContext;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;

@RunWith(MockitoJUnitRunner.class)
public class DockerProcessStartHookTest {
 
  @Mock
  private DockerFacade dockerFacade;
  
  @Mock
  private ServerContext serverContext;
  
  @Mock
  private DockerClient dockerClient;
  
  @Mock
  private Env env;
  
  @Mock
  private LogCallback callback;
  
  private ContainerCreation creation;
  
  private DockerProcessStartHook hook;
  
  private DockerStarter starter;
  
  private Process process;
  
  @Before
  public void setUp() throws Exception {
    process   = new Process(new DistributionInfo("dist", "1.0", "test", "dockerProcesss"));
    process.setStarterType(StarterType.DOCKER);
    creation  = new ContainerCreation("docker-process-id");
    starter   = new DockerStarter();
    
    hook = new DockerProcessStartHook();
    hook.setDockerFacade(dockerFacade);
    hook.setServerContext(serverContext);
    
    when(dockerFacade.getDockerClient()).thenReturn(dockerClient);
    when(dockerClient.createContainer(any(ContainerConfig.class), anyString())).thenReturn(creation);
    when(env.getProperties()).thenReturn(new Property[]{});
  }

  @Test
  public void testAccepts() {
    assertTrue(hook.accepts(new ProcessContext(process)));
  }
  
  @Test
  public void testAccepts_false() {
    process.setStarterType(StarterType.GENERIC);
    assertFalse(hook.accepts(new ProcessContext(process)));
  }

  @Test
  public void testStart_with_starter_image() throws Exception {
    starter.setImage("test-image");
    StarterResult result = new StarterResult(StarterType.DOCKER, new CmdLine(), false);
    result.set(DockerStarter.DOCKER_STARTER_ATTACHMENT, new DockerStarter.DockerStarterAttachment(env, starter));
    hook.start(new ProcessContext(process), result, callback);
  
    verify(dockerClient).createContainer(any(ContainerConfig.class), anyString());
    verify(dockerClient).startContainer(anyString());
    verify(dockerClient).close();
  }

}
