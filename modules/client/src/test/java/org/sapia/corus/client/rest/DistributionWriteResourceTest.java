package org.sapia.corus.client.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
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
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.UndeployPreferences;

@RunWith(MockitoJUnitRunner.class)
public class DistributionWriteResourceTest {
  
  @Mock
  private CorusConnector connector;
  
  @Mock
  private CorusConnectionContext connection;
  
  @Mock
  private RestRequest    request;
  
  @Mock
  private DeployerFacade deployer;
  
  private DistributionWriteResource resource;
  private RequestContext            context;

  @Before
  public void setUp() {
    resource = new DistributionWriteResource() {
      @Override
      protected File transfer(RequestContext ctx) throws IOException {
        File toReturn = mock(File.class);
        when(toReturn.getAbsolutePath()).thenReturn("test");
        return toReturn;
      }
    };
    context  = new RequestContext(request, connector);
    
    when(connector.getContext()).thenReturn(connection);
    when(connector.getDeployerFacade()).thenReturn(deployer);
    when(request.getValue("corus:host")).thenReturn(new Value("corus:host", "localhost:33000"));
    when(request.getValue("d")).thenReturn(new Value("d", "dist"));
    when(request.getValue("v")).thenReturn(new Value("v", "version"));
    when(request.getValue("backup", "0")).thenReturn(new Value("backup", "0"));
    when(request.getValue("runScripts", "false")).thenReturn(new Value("backup", "0"));
    when(request.getValue("name")).thenReturn(new Value("name", "dist"));
    when(request.getValue("version")).thenReturn(new Value("version", "1.0"));
    when(request.getValue("rev")).thenReturn(new Value("rev", "previous"));
  }
  
  @Test
  public void testDeployDistributionForCluster() throws Exception {
    resource.deployDistributionForCluster(context);
    verify(deployer).deployDistribution(eq("test"), any(DeployPreferences.class), any(ClusterInfo.class));
  }

  @Test
  public void testDeployDistributionForHost() throws Exception {
    resource.deployDistributionForHost(context);
    verify(deployer).deployDistribution(eq("test"), any(DeployPreferences.class), any(ClusterInfo.class));
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
  public void testRollbackDistributionForHost() throws Exception {
    resource.rollbackDistributionForCluster(context);
    verify(deployer).rollbackDistribution(eq("dist"), eq("1.0"), any(ClusterInfo.class));
  }
}
