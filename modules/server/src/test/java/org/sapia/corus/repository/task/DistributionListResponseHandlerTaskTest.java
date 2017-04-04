package org.sapia.corus.repository.task;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.repository.DistributionDeploymentRequest;
import org.sapia.corus.client.services.repository.DistributionListResponse;
import org.sapia.corus.client.services.repository.RepoDistribution;
import org.sapia.corus.repository.PullProcessState;
import org.sapia.ubik.net.ServerAddress;

public class DistributionListResponseHandlerTaskTest extends AbstractRepoTaskTest {
  
  private Deployer                 deployer;
  private RepoDistribution         distribution;
  private Endpoint                 serverEndpoint;
  private DistributionListResponse response;
  private PullProcessState         pullProcessState; 
  private DistributionListResponseHandlerTask task;
 
  @Before
  public void setUp() throws Exception {
    super.doSetUp();
    distribution = new RepoDistribution("test", "1.0");
    serverEndpoint = new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class));
    
    response = new DistributionListResponse(serverEndpoint);
    response.addDistribution(distribution);

    deployer = mock(Deployer.class);
    when(serviceContext.getDeployer()).thenReturn(deployer);
    
    pullProcessState = new PullProcessState();
    task = new DistributionListResponseHandlerTask(response, pullProcessState);
  }
  
  @Test
  public void testAcquireDistribution() throws Throwable {
    when(deployer.getDistribution(any(DistributionCriteria.class))).thenThrow(new DistributionNotFoundException("Not found"));
    
    task.execute(taskContext, null);
    
    verify(this.eventChannel, never()).dispatch(eq(response.getEndpoint().getServerAddress()), eq(DistributionDeploymentRequest.EVENT_TYPE), any(DistributionDeploymentRequest.class));
    assertThat(pullProcessState.getDiscoveredDistributionsFromHost(serverEndpoint.getChannelAddress())).containsOnly(distribution);
  }
  
  @Test
  public void testDistributionAlreadyDiscoveredByAnotherHost() throws Throwable {
    when(deployer.getDistribution(any(DistributionCriteria.class))).thenThrow(new DistributionNotFoundException("Not found"));
    pullProcessState.addDiscoveredDistributionFromHostIfAbsent(distribution, mock(ServerAddress.class));

    task.execute(taskContext, null);
    
    verify(this.eventChannel, never()).dispatch(eq(response.getEndpoint().getServerAddress()), eq(DistributionDeploymentRequest.EVENT_TYPE), any(DistributionDeploymentRequest.class));
    assertThat(pullProcessState.getDiscoveredDistributionsFromHost(serverEndpoint.getChannelAddress())).isEmpty();
  }
  
  @Test
  public void testAlreadyGotDistributionOnServer() throws Throwable {
    when(deployer.getDistribution(any(DistributionCriteria.class))).thenReturn(mock(Distribution.class));

    task.execute(taskContext, null);
    
    verify(this.eventChannel, never()).dispatch(eq(response.getEndpoint().getServerAddress()), eq(DistributionDeploymentRequest.EVENT_TYPE), any(DistributionDeploymentRequest.class));
    assertThat(pullProcessState.getDiscoveredDistributionsFromHost(serverEndpoint.getChannelAddress())).isEmpty();
  }

}
