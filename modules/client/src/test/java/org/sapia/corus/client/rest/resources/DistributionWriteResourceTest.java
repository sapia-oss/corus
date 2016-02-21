package org.sapia.corus.client.rest.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

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
import org.sapia.corus.client.facade.DeployerFacade;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.PartitionService;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.async.AsyncTask;
import org.sapia.corus.client.rest.async.AsynchronousCompletionService;
import org.sapia.corus.client.rest.resources.DistributionWriteResource;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.UndeployPreferences;

@RunWith(MockitoJUnitRunner.class)
public class DistributionWriteResourceTest {
  
  @Mock
  private CorusConnector connector;
  
  @Mock
  private ConnectorPool connectors;
  
  @Mock
  private CorusConnectionContext connection;
  
  @Mock
  private AsynchronousCompletionService  async;
  
  @Mock
  private PartitionService partitions;
  
  @Mock
  private RestRequest request;
  
  @Mock
  private DeployerFacade deployer;
  
  private DistributionWriteResource resource;
  private RequestContext            context;

  @Before
  public void setUp() {
    resource = new DistributionWriteResource() {
      @Override
      protected File transfer(RequestContext ctx, String extension) throws IOException {
        File toReturn = mock(File.class);
        when(toReturn.getAbsolutePath()).thenReturn("test");
        return toReturn;
      }
    };
    context  = new RequestContext(request, connector, async, partitions, connectors);
    
    when(connectors.acquire()).thenReturn(connector);
    when(connector.getContext()).thenReturn(connection);
    when(connector.getDeployerFacade()).thenReturn(deployer);
    when(request.getValue("corus:host")).thenReturn(new Value("corus:host", "localhost:33000"));
    when(request.getValue("d")).thenReturn(new Value("d", "dist"));
    when(request.getValue("v")).thenReturn(new Value("v", "version"));
    when(request.getValue("backup", "0")).thenReturn(new Value("backup", "0"));
    when(request.getValue("checksum-md5")).thenReturn(new Value("checksum-md5", "test-checksum"));
    when(request.getValue("runScripts", "false")).thenReturn(new Value("backup", "0"));
    when(request.getValue(eq("batchSize"), anyString())).thenReturn(new Value("batchSize", "0"));
    when(request.getValue(eq("async"), anyString())).thenReturn(new Value("async", "false"));
    when(request.getValue("minHosts", "1")).thenReturn(new Value("minHosts", "1"));
    when(request.getValue("corus:name")).thenReturn(new Value("corus:name", "dist"));
    when(request.getValue("corus:version")).thenReturn(new Value("corus:version", "1.0"));
    when(request.getValue("rev")).thenReturn(new Value("rev", "previous"));
    when(request.getValue("maxErrors", "0")).thenReturn(new Value("maxErrors", "0"));
    when(request.getValue("async", "false")).thenReturn(new Value("async", "false"));
    when(request.getValue("runDiagnostic", "false")).thenReturn(new Value("runDiagnostic", "false"));
    when(request.getValue("diagnosticInterval", "false")).thenReturn(new Value("diagnosticInterval", "10"));

  }
  
  @Test
  public void testDeployDistributionForCluster() throws Exception {
    resource.deployDistributionForCluster(context);
    verify(deployer).deployDistribution(eq("test"), any(DeployPreferences.class), any(ClusterInfo.class));
  }
  
  @Test
  public void testDeployDistributionForCluster_async() throws Exception {
    when(request.getValue(eq("async"), anyString())).thenReturn(new Value("async", "true"));
    
    resource.deployDistributionForCluster(context);
   
    verify(deployer, never()).deployDistribution(eq("test"), any(DeployPreferences.class), any(ClusterInfo.class));
    verify(async).registerForExecution(any(AsyncTask.class));
  }

  @Test
  public void testDeployDistributionForHost() throws Exception {
    resource.deployDistributionForHost(context);
    verify(deployer).deployDistribution(eq("test"), any(DeployPreferences.class), any(ClusterInfo.class));
  }
  
  @Test
  public void testDeployDistributionForHost_async() throws Exception {
    when(request.getValue(eq("async"), anyString())).thenReturn(new Value("async", "true"));
    
    resource.deployDistributionForHost(context);

    verify(deployer, never()).deployDistribution(eq("test"), any(DeployPreferences.class), any(ClusterInfo.class));
    verify(async).registerForExecution(any(AsyncTask.class));
  }

  @Test
  public void testUndeployDistributionForCluster() throws Exception {
    resource.undeployDistributionForCluster(context);
    verify(deployer).undeployDistribution(any(DistributionCriteria.class), any(UndeployPreferences.class), any(ClusterInfo.class));
  }

  @Test
  public void testUndeployDistributionForHost() throws Exception {
    resource.undeployDistributionForCluster(context);
    verify(deployer).undeployDistribution(any(DistributionCriteria.class), any(UndeployPreferences.class), any(ClusterInfo.class));
  }
  
  @Test
  public void testRollbackDistributionForCluster() throws Exception {
    resource.rollbackDistributionForCluster(context);
    verify(deployer).rollbackDistribution(eq("dist"), eq("1.0"), any(ClusterInfo.class));
  }
  
  @Test
  public void testRollbackDistributionForCluster_async() throws Exception {
    when(request.getValue(eq("async"), anyString())).thenReturn(new Value("async", "true"));

    resource.rollbackDistributionForCluster(context);
    
    verify(deployer, never()).rollbackDistribution(eq("dist"), eq("1.0"), any(ClusterInfo.class));
    verify(async).registerForExecution(any(AsyncTask.class));
  }

  @Test
  public void testRollbackDistributionForHost() throws Exception {
    resource.rollbackDistributionForCluster(context);
    verify(deployer).rollbackDistribution(eq("dist"), eq("1.0"), any(ClusterInfo.class));
  }
  
  @Test
  public void testRollbackDistributionForHost_async() throws Exception {
    when(request.getValue(eq("async"), anyString())).thenReturn(new Value("async", "true"));

    resource.rollbackDistributionForCluster(context);
    
    verify(deployer, never()).rollbackDistribution(eq("dist"), eq("1.0"), any(ClusterInfo.class));
    verify(async).registerForExecution(any(AsyncTask.class));

  }
}
