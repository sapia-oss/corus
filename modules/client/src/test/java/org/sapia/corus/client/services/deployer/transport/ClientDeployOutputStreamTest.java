package org.sapia.corus.client.services.deployer.transport;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.ubik.serialization.SerializationStreams;

@RunWith(MockitoJUnitRunner.class)
public class ClientDeployOutputStreamTest {

  private static final int CONTENT_LEN = 1000;
  
  @Mock
  private DeploymentClient deployClient;
  private byte[] content;
  private ClientDeployOutputStream deployOutput;
  private ByteArrayOutputStream clientOutput;
  private ByteArrayInputStream  clientInput;
 
  private int outputLen, metaLen;
  @Before
  public void setUp() throws IOException {
    content = new byte[CONTENT_LEN];
    for (int i = 0; i < CONTENT_LEN; i++) {
      content[i] = (byte)1;
    }
    
    ClusterInfo info = new ClusterInfo(true);
    DeploymentMetadata meta = new DistributionDeploymentMetadata("test", CONTENT_LEN, DeployPreferences.newInstance(), info);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = SerializationStreams.createObjectOutputStream(bos);
    oos.writeObject(meta);
    oos.flush();
    
    metaLen = bos.toByteArray().length;
    outputLen = CONTENT_LEN + metaLen;
    
    clientOutput = new ByteArrayOutputStream();
    
    bos = new ByteArrayOutputStream();
    oos = SerializationStreams.createObjectOutputStream(bos);
    oos.writeObject(new TestProgressQueue());
    oos.flush();
    
    clientInput = new ByteArrayInputStream(bos.toByteArray());
 
    when(deployClient.getInputStream()).thenReturn(clientInput);
    when(deployClient.getOutputStream()).thenReturn(clientOutput);

    deployOutput = new ClientDeployOutputStream(meta, deployClient);
  }
  
  @Test
  public void testClose() throws IOException {
    deployOutput.close();
  }

  @Test
  public void testCommit() throws IOException {
    deployOutput.commit();
  }

  @Test
  public void testWrite_byte_buffer() throws IOException {
    deployOutput.write(content, 0, content.length);
    assertEquals(outputLen, clientOutput.toByteArray().length);
    assertEquals(content.length, deployOutput.getBytesWritten());
  }

  @Test
  public void testWrite_byte_array() throws IOException {
    deployOutput.write(content);
    assertEquals(outputLen, clientOutput.toByteArray().length);
    assertEquals(content.length, deployOutput.getBytesWritten());
  }

  @Test
  public void testWrite_int() throws IOException {
    deployOutput.write(1);
    assertEquals(metaLen + 1, clientOutput.toByteArray().length);
    assertEquals(1, deployOutput.getBytesWritten());
  }

  public static class TestProgressQueue extends ProgressQueueImpl implements Serializable {
    public TestProgressQueue() {
    }
  }
}
