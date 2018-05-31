package org.sapia.corus.deployer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.transport.DeploymentClient;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DistributionDeploymentMetadata;
import org.sapia.corus.deployer.ResilientDeploymentClient.DeploymentClientSupplier;
import org.sapia.ubik.util.Func;

@RunWith(MockitoJUnitRunner.class)
public class ResilientDeploymentClientTest {
  
  @Mock
  private OutputStream delegateStream;
  
  @Mock
  private DeploymentClient delegateClient;
  
  @Mock
  private DeploymentClientSupplier supplier;
  
  @Mock
  private Func<List<ProgressMsg>, IOException> errorCallback;
  
  private ResilientDeploymentClient client;
  
  
  @Before
  public 
  void setUp() throws Exception {
    client = new ResilientDeploymentClient(supplier, errorCallback);

    when(errorCallback.call(any())).thenReturn(Arrays.asList(new ProgressMsg("Error", ProgressMsg.ERROR)));
    when(delegateClient.getOutputStream()).thenReturn(delegateStream);
    when(supplier.getClient()).thenReturn(delegateClient);
    
  }
  
  @Test
  public void testGetClient_error() throws IOException {
    doThrow(new IOException("I/O error")).when(supplier).getClient();
    
    client.getOutputStream().write(new byte[] {});
    client.getOutputStream().write(new byte[] {}, 0, 10);
    
    verify(errorCallback).call(any());
  }
  
  public void testDeploy_error() throws IOException {
    doThrow(new IOException("I/O error")).when(client).deploy(any(), any());
    
    DeploymentMetadata meta = new DistributionDeploymentMetadata(
        "test-file", 
        1000, 
        DeployPreferences.newInstance(),
        ClusterInfo.clustered()
    );
    
    ProgressQueue progress = client.deploy(meta, new ByteArrayInputStream(new byte[0]));
    
    assertTrue(progress.hasNext());
    ProgressMsg msg = progress.fetchNext().get(0);
    assertEquals("Error", msg.getMessage());
    assertEquals(ProgressMsg.WARNING, msg.getStatus());
    
    verify(errorCallback).call(any());
  }
  
  @Test
  public void testWrite_with_bytes() throws IOException {
    doThrow(new IOException("I/O error")).when(delegateStream).write(any(byte[].class));
    
    OutputStream out = client.getOutputStream();
    out.write(new byte[] {});
    out.write(new byte[] {});
    
    verify(errorCallback).call(any());
  }

  @Test
  public void testWrite_with_bytes_at_offset() throws IOException {
    doThrow(new IOException("I/O error")).when(delegateStream).write(any(byte[].class), anyInt(), anyInt());
    
    OutputStream out = client.getOutputStream();
    out.write(new byte[] {}, 0, 10);
    out.write(new byte[] {}, 0, 10);
    
    verify(errorCallback).call(any());
  }

  @Test
  public void testWrite_with_int() throws IOException {
    doThrow(new IOException("I/O error")).when(delegateStream).write(anyInt());
    
    OutputStream out = client.getOutputStream();
    out.write(10);
    out.write(10);
    
    verify(errorCallback).call(any());
    
  }

  @Test
  public void testFlush() throws IOException {
    doThrow(new IOException("I/O error")).when(delegateStream).flush();
    
    OutputStream out = client.getOutputStream();
    out.flush();
    out.flush();
    
    verify(errorCallback).call(any()); 
  }

  @Test
  public void testClose_stream() throws IOException {
    doThrow(new IOException("I/O error")).when(delegateStream).close();
    
    OutputStream out = client.getOutputStream();
    out.close();
    out.close();
    
    verify(errorCallback).call(any()); 
  }

}
