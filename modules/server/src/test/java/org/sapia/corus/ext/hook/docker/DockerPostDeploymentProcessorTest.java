package org.sapia.corus.ext.hook.docker;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter;
import org.sapia.corus.deployer.processor.DeploymentContext;
import org.sapia.corus.docker.DockerClientFacade;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class DockerPostDeploymentProcessorTest {
  
  @Mock
  private Configurator configurator;
  
  @Mock
  private DockerClientFacade dockerClient;
  
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
    when(dockerFacade.isAutoRemoveEnabled()).thenReturn(true);
    when(dockerFacade.isRegistrySyncEnabled()).thenReturn(true);
    when(dockerFacade.isEnabled()).thenReturn(true);
    when(dockerClient.checkContainsImages(anySetOf(String.class))).thenReturn(new HashSet<String>());
  }

  @Test
  public void testOnPostDeploy() throws Exception {
    when(configurator.getTags()).thenReturn(Tag.asTags(Collects.arrayToSet("dt1", "dt2", "pct1", "pct2")));
    processor.onPostDeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient).pullImage(eq("test-img"), any(LogCallback.class));
    verify(dockerClient, never()).checkContainsImages(anySetOf(String.class));
  }
  
  @Test
  public void testOnPostDeploy_facade_disabled() throws Exception {
    when(dockerFacade.isEnabled()).thenReturn(false);
    when(configurator.getTags()).thenReturn(Tag.asTags(Collects.arrayToSet("dt1", "dt2", "pct1", "pct2")));
    processor.onPostDeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient, never()).pullImage(eq("test-img"), any(LogCallback.class));
  }
  
  @Test
  public void testOnPostDeploy_distribution_image() throws Exception {
    image = OptionalValue.none();
    when(configurator.getTags()).thenReturn(Tag.asTags(Collects.arrayToSet("dt1", "dt2", "pct1", "pct2")));
    processor.onPostDeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient).pullImage(eq("test:1.0"), any(LogCallback.class));
    verify(dockerClient, never()).checkContainsImages(anySetOf(String.class));
  }
  
  @Test
  public void testOnPostDeploy_no_tag_match() throws Exception {
    processor.onPostDeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient, never()).pullImage(eq("test-img"), any(LogCallback.class));
    verify(dockerClient, never()).checkContainsImages(anySetOf(String.class));
  }
  
  @Test
  public void testOnPostDeploy_registry_sync_disabled() throws Exception {
    when(dockerFacade.isRegistrySyncEnabled()).thenReturn(false);
    processor.onPostDeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient, never()).pullImage(anyString(), any(LogCallback.class));
    verify(dockerClient).checkContainsImages(anySetOf(String.class));
  }
  
  @Test
  public void testOnPostUndeploy() throws Exception {
    
    when(configurator.getTags()).thenReturn(Tag.asTags(Collects.arrayToSet("dt1", "dt2", "pct1", "pct2")));
    processor.onPostUndeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient).removeImage(eq("test-img"), any(LogCallback.class));
  }
  
  @Test
  public void testOnPostUndeploy_facade_disabled() throws Exception {
    when(dockerFacade.isEnabled()).thenReturn(false);
    when(configurator.getTags()).thenReturn(Tag.asTags(Collects.arrayToSet("dt1", "dt2", "pct1", "pct2")));
    processor.onPostUndeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient, never()).removeImage(eq("test-img"), any(LogCallback.class));
  }
  
  @Test
  public void testOnPostUndeploy_auto_remove_disabled() throws Exception {
    when(dockerFacade.isAutoRemoveEnabled()).thenReturn(false);
    when(configurator.getTags()).thenReturn(Tag.asTags(Collects.arrayToSet("dt1", "dt2", "pct1", "pct2")));
    processor.onPostUndeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient, never()).removeImage(eq("test-img"), any(LogCallback.class));
  }
  
  @Test
  public void testOnPostUndeploy_distribution_image() throws Exception {
    image = OptionalValue.none();
    when(configurator.getTags()).thenReturn(Tag.asTags(Collects.arrayToSet("dt1", "dt2", "pct1", "pct2")));
    processor.onPostUndeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient).removeImage(eq("test:1.0"), any(LogCallback.class));
  }

  @Test
  public void testOnPostUndeploy_no_tag_match() throws Exception {
    processor.onPostUndeploy(new DeploymentContext(createDistribution(image)), mock(LogCallback.class));
    verify(dockerClient, never()).removeImage(eq("test-img"), any(LogCallback.class));
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
