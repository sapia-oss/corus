package org.sapia.corus.ftest.docker;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.SafeProperties;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.docker.SpotifyDockerFacade;
import org.sapia.corus.util.PropertiesUtil;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.spotify.docker.client.messages.Container;

public class SpotifyDockerFacadeFuncTest {
  
  private static final int CONTAINER_STOP_TIMEOUT = 30;
  
  private static final int BUFSZ = 2048;

  private static final String BUSY_BOX = "busybox:latest";
  
  private InternalConfigurator configurator;
  
  private ServerContext serverContext;

  private LogCallback logCallback;

  private SpotifyDockerFacade sut;
  
  private OptionalValue<String> containerId = OptionalValue.none();
    
  @BeforeSuite
  public void beforeSuite() throws IOException {
    configurator  = mock(InternalConfigurator.class);
    serverContext = mock(ServerContext.class);
    logCallback   = new LogCallback() {
      @Override
      public void info(String msg) {
        System.out.println("[INFO] " + msg);
      }
      
      @Override
      public void error(String msg) {
        System.out.println("[ERROR] " + msg);
        throw new IllegalStateException(msg);
      }
      
      @Override
      public void debug(String msg) {
        System.out.println("[DEBUG] " + msg);
      }
    };
    sut = new SpotifyDockerFacade();
    sut.setServerContext(serverContext);
    
    File userPropFile = FilePath.newInstance()
        .addCorusUserDir()
        .setRelativeFile("corus.properties")
        .createFile();
    
    SafeProperties userProps = new SafeProperties(System.getProperties());
    PropertiesUtil.loadIfExist(userProps, userPropFile);

    sut.setEnabled(true);
    OptionalValue<String> certPath = userProps.getOptionalProperty(CorusConsts.PROPERTY_CORUS_DOCKER_CERTIFICATES_PATH);
    if (certPath.isSet()) {
        sut.setCertificatesPath(certPath.get());
    }
    sut.setDaemonUri(userProps.getProperty(CorusConsts.PROPERTY_CORUS_DOCKER_DAEMON_URI, 
        "unix:///var/run/docker.sock"));
    sut.setEmail(userProps.getPropertyNotNull(CorusConsts.PROPERTY_CORUS_DOCKER_CLIENT_EMAIL));
    sut.setPassword(userProps.getPropertyNotNull(CorusConsts.PROPERTY_CORUS_DOCKER_CLIENT_PASSWORD));
    sut.setServerAddress(userProps.getProperty(CorusConsts.PROPERTY_CORUS_DOCKER_REGISTRY_ADDRESS, 
        "https://registry.hub.docker.com/v1/repositories"));
    sut.setUsername(userProps.getPropertyNotNull(CorusConsts.PROPERTY_CORUS_DOCKER_CLIENT_USERNAME));
    
    sut.setConfigurator(configurator);
    sut.setServerContext(serverContext);
    sut.init();
    
    sut.clean();
  }
  
  @AfterSuite
  public void afterSuite() {
    sut.shutdown();
  }
  
  @BeforeMethod
  public void beforeTest() {
    sut.setEnabled(true);
  }
  
  // --------------------------------------------------------------------------
  // Misc
  
  @Test(expectedExceptions = IllegalStateException.class)
  public void test_docker_facade_disabled() throws Exception {
    sut.setEnabled(false);
    sut.getDockerClient().pullImage("test/image:1.0", logCallback);
  }
  
  // --------------------------------------------------------------------------
  // Pull
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPullImage_null_image_name() throws Exception {
    sut.getDockerClient().pullImage(null, logCallback);
  }

  @Test
  public void testPullImage() throws Exception {
    sut.getDockerClient().pullImage(BUSY_BOX, logCallback);
    
    assertEquals(sut.getAllImages().size(), 1);
  }
  
  // --------------------------------------------------------------------------
  // Save
  @Test(dependsOnMethods = "testPullImage")
  public void testSaveImage() throws Exception {
    File imageFile = FilePath.newInstance()
        .addJvmTempDir()
        .setRelativeFile("docker_func_test_" + System.nanoTime() + ".tar")
        .createFile();
    
    imageFile.deleteOnExit();
    byte[] buf  = new byte[BUFSZ];
    int    read = 0;;
    try (OutputStream ios = new BufferedOutputStream(new FileOutputStream(imageFile), BUFSZ)) {
      try (InputStream iis = new BufferedInputStream(sut.getDockerClient().saveImage(BUSY_BOX, logCallback), BUFSZ)) {
        while ((read = iis.read(buf, 0, BUFSZ)) > -1) {
          ios.write(buf, 0, read);
        }
      }
    }
    
    assertTrue("Saved image file does not exist", imageFile.exists());
    assertTrue("Saved image file is empty", imageFile.length() > 0);
  }
  
  // --------------------------------------------------------------------------
  // Start
  
  @Test(expectedExceptions = IllegalArgumentException.class, dependsOnMethods = "testSaveImage")
  public void testStartContainer_null_container_id() throws Exception {
    sut.getDockerClient().startContainer(null, logCallback);
  }

  @Test(dependsOnMethods = "testSaveImage")
  public void testStartContainer() throws Exception {
    String newContainerId = sut.getDockerClient().startContainer(BUSY_BOX, logCallback);
    this.containerId = OptionalValue.of(newContainerId);
  }

  // --------------------------------------------------------------------------
  // Stop
  
  @Test(expectedExceptions = IllegalArgumentException.class, dependsOnMethods = "testStartContainer") 
  public void testStoptContainer_null_container_id() throws Exception {
    sut.getDockerClient().stopContainer(null, CONTAINER_STOP_TIMEOUT, logCallback);
  }

  @Test(dependsOnMethods = "testStartContainer")
  public void testStopContainer() throws Exception {
    sut.getDockerClient().stopContainer(containerId.get(), CONTAINER_STOP_TIMEOUT, logCallback);
  }
  
  // --------------------------------------------------------------------------
  // Remove

  @Test(expectedExceptions = IllegalArgumentException.class, dependsOnMethods = "testStopContainer")
  public void testRemoveImage_null_image_name() throws Exception {
    sut.getDockerClient().removeImage(null, logCallback);
  }
  
  @Test(dependsOnMethods = "testStopContainer")
  public void testRemoveImages() throws Exception {
    sut.stopAllContainers();
    sut.getDockerClient().removeImage(BUSY_BOX, logCallback);
   
    assertEquals(sut.getAllImages().size(), 0);
  }
}
