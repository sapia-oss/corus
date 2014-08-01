package org.sapia.corus.deployer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DistributionDeploymentMetadata;

@RunWith(MockitoJUnitRunner.class)
public class DeployOutputStreamImplTest {

  private static final int CONTENT_LEN = 1000;
  private byte[] content;
  @Mock
  private DeploymentHandler handler;
  private DeployOutputStreamImpl deployOutput;
  private File file;

  @Before
  public void setUp() throws IOException {
    content = new byte[CONTENT_LEN];
    for (int i = 0; i < CONTENT_LEN; i++) {
      content[i] = (byte)1;
    }
    ClusterInfo info = new ClusterInfo(true);
    DeploymentMetadata meta = new DistributionDeploymentMetadata("test", CONTENT_LEN, info);
    file = File.createTempFile("corus-test-" + UUID.randomUUID().toString(), "dat");
    file.deleteOnExit();
    deployOutput = new DeployOutputStreamImpl(file, meta, handler);
    
    when(handler.completeDeployment(any(DeploymentMetadata.class), any(File.class))).thenReturn(new ProgressQueueImpl());
  }
  
  
  @Test
  public void testWrite_int() throws IOException {
    deployOutput.write(1);
    deployOutput.close();
    assertEquals(1, deployOutput.getBytesWritten());
    assertEquals(1, written().length);
  }

  @Test
  public void testWrite_byte_array() throws IOException {
    deployOutput.write(content);
    deployOutput.close();
    assertEquals(CONTENT_LEN, deployOutput.getBytesWritten());
    assertEquals(CONTENT_LEN, written().length);
  }

  @Test
  public void testWrite_byte_buffer() throws IOException {
    deployOutput.write(content, 0, content.length);
    deployOutput.close();
    assertEquals(CONTENT_LEN, deployOutput.getBytesWritten());
    assertEquals(CONTENT_LEN, written().length);
  }

  @Test
  public void testClose() throws IOException {
    deployOutput.close();
  }

  @Test
  public void testCommit() throws IOException {
    deployOutput.commit();
  }
  
  public byte[] written() throws IOException {
    ByteArrayOutputStream written = new ByteArrayOutputStream();
    int read;
    byte[] buf = new byte[CONTENT_LEN];
    FileInputStream fis = new FileInputStream(file);
    try {
      while ((read = fis.read(buf, 0, buf.length)) > -1) {
        written.write(buf, 0, read);
      }
      written.flush();
      written.close();
    } finally {
      fis.close();
    }
    return written.toByteArray();
  }

}
