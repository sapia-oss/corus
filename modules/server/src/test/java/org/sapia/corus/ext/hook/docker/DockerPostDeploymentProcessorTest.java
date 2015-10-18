package org.sapia.corus.ext.hook.docker;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.LogCallback;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter;
import org.sapia.corus.deployer.processor.DeploymentContext;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.ubik.util.Collects;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ProgressHandler;

@RunWith(MockitoJUnitRunner.class)
public class DockerPostDeploymentProcessorTest {
  
  @Mock
  private Configurator configurator;
  
  @Mock
  private DockerClient dockerClient;
  
  @Mock
  private DockerFacade dockerFacade;
  
  private OptionalValue<String> image;
  
  private DockerPostDeploymentProcessor processor;
  
  @Before
  public void setUp() throws Exception {
    image = OptionalValue.of("test-img");
    processor = new DockerPostDeploymentProcessor();
    processor.setConfigurator(configurator);
    processor.setDockerFacade(dockerFacade);
    
    when(dockerFacade.getDockerClient()).thenReturn(dockerClient);
  }

  @Test
  public void testOnPostDeploy() throws Exception {
    when(configurator.getTags()).thenReturn(Tag.asTags(Collects.arrayToSet("dt1", "dt2", "pct1", "pct2")));
    processor.onPostDeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient).pull(eq("test-img"), any(ProgressHandler.class));
  }
  
  @Test
  public void testOnPostDeploy_distribution_image() throws Exception {
    image = OptionalValue.none();
    when(configurator.getTags()).thenReturn(Tag.asTags(Collects.arrayToSet("dt1", "dt2", "pct1", "pct2")));
    processor.onPostDeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient).pull(eq("test:1.0"), any(ProgressHandler.class));
  }
  
  @Test
  public void testOnPostDeploy_no_tag_match() throws Exception {
    processor.onPostDeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient, never()).pull(eq("test-img"), any(ProgressHandler.class));
  }
  
  @Test
  public void testOnPostUndeploy() throws Exception {
    when(configurator.getTags()).thenReturn(Tag.asTags(Collects.arrayToSet("dt1", "dt2", "pct1", "pct2")));
    processor.onPostDeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient).pull(eq("test-img"), any(ProgressHandler.class));
  }
  
  @Test
  public void testOnPostUndeploy_distribution_image() throws Exception {
    image = OptionalValue.none();
    when(configurator.getTags()).thenReturn(Tag.asTags(Collects.arrayToSet("dt1", "dt2", "pct1", "pct2")));
    processor.onPostDeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient).pull(eq("test:1.0"), any(ProgressHandler.class));
  }

  @Test
  public void testOnPostUndeploy_no_tag_match() throws Exception {
    processor.onPostDeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient, never()).pull(eq("test-img"), any(ProgressHandler.class));
  }

  private Distribution createDistribution(OptionalValue<String> image) {
    Distribution dist = new Distribution("test", "1.0");
    dist.setTags("dt1,dt2");
    
    ProcessConfig pc = new ProcessConfig("test-proc");
    pc.setTags("pct1, pct2");
    pc.setName("");
    dist.addProcess(pc);
 
    DockerStarter dst = new DockerStarter();
    if (image.isSet()) {
      dst.setImage(image.get());
    }
    pc.addStarter(dst);
    
    return dist;
    
  }
}
