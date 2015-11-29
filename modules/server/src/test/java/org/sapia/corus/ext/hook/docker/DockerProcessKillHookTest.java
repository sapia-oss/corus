package org.sapia.corus.ext.hook.docker;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.deployer.dist.StarterType;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.docker.DockerClientFacade;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.corus.processor.hook.ProcessContext;

@RunWith(MockitoJUnitRunner.class)
public class DockerProcessKillHookTest {
  
  @Mock
  private DockerFacade dockerFacade;
  
  @Mock
  private ServerContext serverContext;
  
  @Mock
  private DockerClientFacade dockerClient;
  
  @Mock
  private ProcessorConfiguration processorConf;
  
  @Mock
  private LogCallback callback;
  
  private DockerProcessKillHook hook;
  
  private Process process;
  
  @Before
  public void setUp() throws Exception {
    process   = new Process(new DistributionInfo("dist", "1.0", "test", "dockerProcesss"));
    process.setStarterType(StarterType.DOCKER);
    
    hook = new DockerProcessKillHook();
    hook.setDockerFacade(dockerFacade);
    hook.setProcessorConfig(processorConf);
    
    when(processorConf.getKillIntervalMillis()).thenReturn(1000L);
    when(dockerFacade.getDockerClient()).thenReturn(dockerClient);
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
  public void testKill() throws Exception {
    hook.kill(new ProcessContext(process), KillSignal.SIGTERM, callback);
  
    verify(dockerClient).stopContainer(eq(process.getOsPid()), anyInt(), any(LogCallback.class));
  }
}
