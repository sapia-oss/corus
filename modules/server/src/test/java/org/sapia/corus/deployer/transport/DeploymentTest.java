package org.sapia.corus.deployer.transport;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.services.deployer.transport.ByteArrayDeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DistributionDeploymentMetadata;
import org.sapia.corus.core.CorusTransport;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.serialization.SerializationStreams;

public class DeploymentTest {
  
  private ServerContext  context;
  private CorusTransport transport;
  private ServerAddress  address;
  
  @Before
  public void setUp() {
    context = mock(ServerContext.class);
    transport = mock(CorusTransport.class);
    address = mock(ServerAddress.class);
    when(context.getTransport()).thenReturn(transport);
    when(transport.getServerAddress()).thenReturn(address);
    when(address.getTransportType()).thenReturn("test");
  }
  
  @Test
  public void testGetContent() throws Exception{
  	ByteArrayOutputStream bos = new ByteArrayOutputStream();
  	byte[] data = new String("THIS IS DATA").getBytes();
  	DeploymentMetadata meta = new DistributionDeploymentMetadata("test", data.length, new ClusterInfo(true));
    ObjectOutputStream oos = SerializationStreams.createObjectOutputStream(bos);
    oos.writeObject(meta);
    bos.write(data);
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    Deployment depl = new Deployment(context, new TestConnection(bis, new ByteArrayOutputStream())) {
      @Override
      ObjectOutputStream createObjectOutputStream(OutputStream os) throws IOException {
        return SerializationStreams.createObjectOutputStream(os);
      }
    };
    
    meta = depl.getMetadata();
    assertEquals("test", meta.getFileName());
		assertEquals(data.length, meta.getContentLength());    
		
		ByteArrayDeployOutputStream deployOutput = new ByteArrayDeployOutputStream();
		depl.deploy(deployOutput);
		
		byte[] outputData = deployOutput.toByteArray();
    String dataStr = new String(outputData);
    assertEquals("THIS IS DATA", dataStr);
  }
}
