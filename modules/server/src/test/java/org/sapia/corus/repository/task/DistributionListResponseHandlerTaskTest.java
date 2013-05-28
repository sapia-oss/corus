package org.sapia.corus.repository.task;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;

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
import org.sapia.ubik.net.ServerAddress;

public class DistributionListResponseHandlerTaskTest extends AbstractRepoTaskTest {
  
  private Deployer                 deployer;
  private DistributionListResponse response;
  private DistributionListResponseHandlerTask task;
 
  @Before
  public void setUp() throws Exception {
    super.doSetUp();
    response = new DistributionListResponse(
        new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class))
    );
    response.addDistribution(new RepoDistribution("test", "1.0"));

    deployer = mock(Deployer.class);
    when(serviceContext.getDeployer()).thenReturn(deployer);
    when(deployer.getDistribution(any(DistributionCriteria.class))).thenThrow(new DistributionNotFoundException("Not found"));
    
    task = new DistributionListResponseHandlerTask(response);

  }
  
  @Test
  public void testAcquireDistribution() throws Throwable {
    task.execute(taskContext, null);
    verify(this.eventChannel, never()).dispatch(eq(response.getEndpoint().getServerAddress()), eq(DistributionDeploymentRequest.EVENT_TYPE), any(DistributionDeploymentRequest.class));
  }
  
  @Test
  public void testAlreadyGotDistribution() throws Throwable {
    Distribution dist = mock(Distribution.class);
    deployer = mock(Deployer.class);
    when(deployer.getDistribution(any(DistributionCriteria.class))).thenReturn(dist);

    task.execute(taskContext, null);
    verify(this.eventChannel, never()).dispatch(eq(response.getEndpoint().getServerAddress()), eq(DistributionDeploymentRequest.EVENT_TYPE), any(DistributionDeploymentRequest.class));
  }

}
