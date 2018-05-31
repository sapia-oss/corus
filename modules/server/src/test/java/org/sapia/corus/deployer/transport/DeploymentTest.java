package org.sapia.corus.deployer.transport;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.event.DeploymentStreamingCompletedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentStreamingStartingEvent;
import org.sapia.corus.client.services.deployer.transport.ByteArrayDeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DistributionDeploymentMetadata;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.core.CorusTransport;
import org.sapia.corus.core.InternalServiceContext;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.serialization.SerializationStreams;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentTest {
  
  @Mock
  private ServerContext  context;
  
  @Mock
  private InternalServiceContext services;
  
  @Mock
  private CorusTransport transport;
  
  @Mock
  private ServerAddress  address;
 
  @Mock
  private EventDispatcher dispatcher;
  
  @Before
  public void setUp() {
    when(context.getTransport()).thenReturn(transport);
    when(transport.getServerAddress()).thenReturn(address);
    when(address.getTransportType()).thenReturn("test");
    when(context.getServices()).thenReturn(services);
    when(services.getEventDispatcher()).thenReturn(dispatcher);
  }
  
  @Test
  public void testGetContent() throws Exception{
  	ByteArrayOutputStream bos = new ByteArrayOutputStream();
  	byte[] data = new String("THIS IS DATA").getBytes();
  	DeploymentMetadata meta = new DistributionDeploymentMetadata("test", data.length, DeployPreferences.newInstance(), new ClusterInfo(true));
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
    
    verify(dispatcher).dispatch(isA(DeploymentStreamingStartingEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentStreamingCompletedEvent.class));
    
  }
}
