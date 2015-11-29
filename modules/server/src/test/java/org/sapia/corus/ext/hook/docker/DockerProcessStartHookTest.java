package org.sapia.corus.ext.hook.docker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.deployer.dist.Property;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.deployer.dist.StarterType;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter.DockerStarterAttachment;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.docker.DockerClientFacade;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.corus.processor.hook.ProcessContext;

@RunWith(MockitoJUnitRunner.class)
public class DockerProcessStartHookTest {
 
  @Mock
  private DockerFacade dockerFacade;
  
  @Mock
  private ServerContext serverContext;
  
  @Mock
  private DockerClientFacade dockerClient;
  
  @Mock
  private Env env;
  
  @Mock
  private LogCallback callback;
    
  private DockerProcessStartHook hook;
  
  private DockerStarter starter;
  
  private Process process;
  
  @Before
  public void setUp() throws Exception {
    process   = new Process(new DistributionInfo("dist", "1.0", "test", "dockerProcesss"));
    process.setStarterType(StarterType.DOCKER);
    starter   = new DockerStarter();
    
    hook = new DockerProcessStartHook();
    hook.setDockerFacade(dockerFacade);
    hook.setServerContext(serverContext);
    
    when(dockerFacade.getDockerClient()).thenReturn(dockerClient);
    when(dockerClient.startContainer(any(ProcessContext.class), any(StarterResult.class), any(DockerStarterAttachment.class), any(LogCallback.class)))
      .thenReturn("test-container-id");
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
  
    verify(dockerClient).startContainer(any(ProcessContext.class), any(StarterResult.class), any(DockerStarterAttachment.class), any(LogCallback.class));
    assertEquals("test-container-id", process.getOsPid());
  }

}
