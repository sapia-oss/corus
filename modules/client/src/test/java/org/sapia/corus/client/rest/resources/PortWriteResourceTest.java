package org.sapia.corus.client.rest.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.rest.RestRequest;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.facade.PortManagementFacade;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.PartitionService;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.async.AsynchronousCompletionService;
import org.sapia.corus.client.rest.resources.PortWriteResource;

@RunWith(MockitoJUnitRunner.class)
public class PortWriteResourceTest {

  @Mock
  private CorusConnector connector;
  
  @Mock
  private ConnectorPool connectors;
  
  @Mock
  private CorusConnectionContext connection;
  
  @Mock
  private AsynchronousCompletionService async;
  
  @Mock
  private PartitionService partitions;
  
  @Mock
  private RestRequest    request;
  
  @Mock
  private PortManagementFacade facade;
  
  private PortWriteResource resource;
  private RequestContext    context;
  
  @Before
  public void setUp() {
    resource = new PortWriteResource();
    
    context  = new RequestContext(request, connector, async, partitions, connectors);
    
    when(connectors.acquire()).thenReturn(connector);
    when(connector.getContext()).thenReturn(connection);
    when(connector.getPortManagementFacade()).thenReturn(facade);
    when(request.getValue("corus:host")).thenReturn(new Value("corus:host", "localhost:33000"));
    when(request.getValue("corus:rangeName")).thenReturn(new Value("corus:rangeName", "test"));
    when(request.getValue("min")).thenReturn(new Value("min", "100"));
    when(request.getValue("max")).thenReturn(new Value("min", "200"));
    when(request.getValue("force", "false")).thenReturn(new Value("force", "true"));
  }
  
  @Test
  public void testAddPortRangeForCluster() throws Exception {
    resource.addPortRangeForCluster(context);
    verify(facade).addPortRange(eq("test"), eq(100), eq(200), any(ClusterInfo.class));
  }

  @Test
  public void testAddPortRangeForHost() throws Exception {
    resource.addPortRangeForHost(context);
    verify(facade).addPortRange(eq("test"), eq(100), eq(200), any(ClusterInfo.class));
  }

  @Test
  public void testDeletePortRangeForCluster() throws Exception {
    resource.deletePortRangeForCluster(context);
    verify(facade).removePortRange(eq("test"), eq(true), any(ClusterInfo.class));
  }

  @Test
  public void testDeletePortRangeForHost() throws Exception {
    resource.deletePortRangeForCluster(context);
    verify(facade).removePortRange(eq("test"), eq(true), any(ClusterInfo.class));
  }

  @Test
  public void testReleasePortRangeForCluster() {
    resource.releasePortRangeForCluster(context);
    verify(facade).releasePortRange(eq("test"), any(ClusterInfo.class));
  }

  @Test
  public void testReleasePortRangeForHost() {
    resource.releasePortRangeForHost(context);
    verify(facade).releasePortRange(eq("test"), any(ClusterInfo.class));
  }

}
