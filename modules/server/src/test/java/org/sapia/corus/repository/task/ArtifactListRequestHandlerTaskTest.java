package org.sapia.corus.repository.task;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.repository.ArtifactListRequest;
import org.sapia.corus.client.services.repository.DistributionListResponse;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.util.Queue;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;

public class ArtifactListRequestHandlerTaskTest extends AbstractRepoTaskTest {
 
  private Endpoint                       ep1, ep2;
  private Queue<ArtifactListRequest>     requests;
  private Distribution                   dist1, dist2;
  private List<Distribution>             distributions;
  private Deployer                       deployer;
  private ArtifactListRequestHandlerTask task;
  private RepositoryConfiguration        conf;
  
  @Before
  public void setUp() throws Exception {
    super.doSetUp();
    
    conf = mock(RepositoryConfiguration.class);
    
    ep1 = new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class));
    ep2 = new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class));
    
    deployer = mock(Deployer.class);
    
    dist1         = new Distribution("dist1", "1.0");
    dist2         = new Distribution("dist2", "1.0");
    distributions = Collects.arrayToList(dist1, dist2);
    
    requests = new Queue<ArtifactListRequest>();

    task =  new ArtifactListRequestHandlerTask(conf, requests);
    
    when(serviceContext.getDeployer()).thenReturn(deployer);
    when(deployer.getDistributions(any(DistributionCriteria.class))).thenReturn(distributions);
  }

  @Test
  public void testSendDistributionListResponse() throws Throwable {
    requests.add(new ArtifactListRequest(ep1));
    requests.add(new ArtifactListRequest(ep2));
    
    task.execute(taskContext, null);
    
    verify(eventChannel, times(1)).dispatch(eq(ep1.getChannelAddress()), eq(DistributionListResponse.EVENT_TYPE), any(DistributionListResponse.class));
    verify(eventChannel, times(1)).dispatch(eq(ep2.getChannelAddress()), eq(DistributionListResponse.EVENT_TYPE), any(DistributionListResponse.class));
  }

  @Test
  public void testSendDistributionListResponse_fo_empty_queue() throws Throwable {
    task.execute(taskContext, null);
    
    verify(eventChannel, never()).dispatch(any(ServerAddress.class), any(), any());
  }
  
  @Test
  public void testSendDistributionListResponse_with_force() throws Throwable {
    requests.add(new ArtifactListRequest(ep1).setForce(true));
    task.execute(taskContext, null);
    
    ArgumentCaptor<DistributionListResponse> captor = ArgumentCaptor.forClass(DistributionListResponse.class);
    verify(eventChannel).dispatch(any(ServerAddress.class), any(), captor.capture());
    assertThat(captor.getValue().isForce()).isTrue();
  }
}
