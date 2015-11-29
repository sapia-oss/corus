package org.sapia.corus.docker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.configurator.InternalConfigurator;

@RunWith(MockitoJUnitRunner.class)
public class SpotifyDockerClientFacadeTest {

  @Mock
  private InternalConfigurator configurator;

  @Mock
  private LogCallback logCallback;

  private SpotifyDockerFacade sut;

  @Before
  public void setUp() throws Exception {
    sut = new SpotifyDockerFacade(configurator);
    sut.setEnabled(false);
    sut.setEmail("jcdesrochers@gmail.com");
    sut.setUsername("jcdesrochers");
    sut.setPassword("B0nj0ur!dh");
    sut.setServerAddress("http://my-local.docker-registry/v1/");

// Use this for local docker on linux
//    sut.setDaemonUri("unix:///var/run/docker.sock");

 // Use this for docker-machine named 'corus'
    sut.setDaemonUri("https://192.168.99.100:2376");
    sut.setCertificatesPath(System.getProperty("user.home") + "/.docker/machine/machines/corus");
    sut.init();
  }

  @After
  public void tearDown() {
    if (sut != null) {
      sut.shutdown();
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPullImage_nullImageName() throws Exception {
    sut.pullImage(null, logCallback);
  }

  @Test(expected = IllegalStateException.class)
  public void testPullImage_withImageName_disabled() throws Exception {
    sut.pullImage("test/image:1.0", logCallback);
  }

  @Test
  public void testPullImage_withImageName_enabled() throws Exception {
    sut.setEnabled(true);
//    sut.pullImage("busybox:latest", logCallback);
    sut.pullImage("jcdesrochers/simple-webapp:latest", logCallback);
  }


  @Test(expected = IllegalArgumentException.class)
  public void testRemoveImage_nullImageName() throws Exception {
    sut.removeImage(null, logCallback);
  }

  @Test(expected = IllegalStateException.class)
  public void testRemoveImage_withImageName_disabled() throws Exception {
    sut.removeImage("test/image:1.0", logCallback);
  }

  @Test
  public void testRemoveImage_withImageName_enabled() throws Exception {
    sut.setEnabled(true);
//    sut.removeImage("busybox:latest", logCallback);
    sut.removeImage("jcdesrochers/simple-webapp:latest", logCallback);
  }



  @Test(expected = IllegalArgumentException.class)
  public void testStartContainer_nullContainerId() throws Exception {
    sut.startContainer(null, logCallback);
  }

  @Test(expected = IllegalStateException.class)
  public void testStartContainer_withContainerId_disabled() throws Exception {
    sut.startContainer("test/image:1.0", logCallback);
  }

  @Test
  public void testStartContainer_withContainerId_enabled() throws Exception {
    sut.setEnabled(true);
    sut.startContainer("57490cb646c6c90d1f8cc124f18bee9c09d5e6f955ec88015237918b6857c238", logCallback);
  }



  @Test(expected = IllegalArgumentException.class)
  public void testStopContainer_nullContainerId() throws Exception {
    sut.stopContainer(null, 1, logCallback);
  }

  @Test(expected = IllegalStateException.class)
  public void testStopContainer_withContainerId_disabled() throws Exception {
    sut.stopContainer("whatever", 1, logCallback);
  }

  @Test(expected = DockerFacadeException.class)
  public void testStopContainer_invalidContainerId_enabled() throws Exception {
    sut.setEnabled(true);
    sut.stopContainer("invalid-container-id", 1, logCallback);
  }

  @Test
  public void testStopContainer_validContainerId_enabled() throws Exception {
    sut.setEnabled(true);
    sut.stopContainer("57490cb646c6", 60, logCallback);
  }



  @Test(expected = IllegalArgumentException.class)
  public void testRemoveContainer_nullContainerId() throws Exception {
    sut.removeContainer(null, logCallback);
  }

  @Test(expected = IllegalStateException.class)
  public void testRemoveContainer_withContainerId_disabled() throws Exception {
    sut.removeContainer("whatever", logCallback);
  }

  @Test(expected = DockerFacadeException.class)
  public void testRemoveContainer_invalidContainerId_enabled() throws Exception {
    sut.setEnabled(true);
    sut.removeContainer("invalid-container-id", logCallback);
  }

  @Test
  public void testRemoveContainer_validContainerId_enabled() throws Exception {
    sut.setEnabled(true);
    sut.removeContainer("57490cb646c6", logCallback);
  }




  @Test
  public void testPing() throws Exception {
    sut.setEnabled(true);
    String actual = sut.ping();

    assertThat(actual).isEqualTo("OK");
  }

  @Test
  public void testVersion() throws Exception {
    sut.setEnabled(true);
    sut.version();
  }

  @Test
  public void testGetAllImages() throws Exception {
    sut.setEnabled(true);
    sut.getAllImages();
  }

}
