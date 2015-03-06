package org.sapia.corus.repository.task.deploy;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.client.services.processor.ProcessDef;
import org.sapia.corus.client.services.repository.DistributionDeploymentRequest;
import org.sapia.corus.client.services.repository.RepoDistribution;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.deployer.InternalDeployer;
import org.sapia.corus.repository.task.AbstractRepoTaskTest;
import org.sapia.corus.taskmanager.util.CompositeTask;
import org.sapia.ubik.net.ServerAddress;

@RunWith(MockitoJUnitRunner.class)
public class DistributionDeploymentRequestHandlerTaskHelperTest extends AbstractRepoTaskTest {

  private DistributionDeploymentRequest            request1, request2;
  private RepoDistribution                         dist1, dist2;
  private List<DistributionDeploymentRequest>      requests;
  private List<ExecConfig>                         execConfigs;
  @Mock
  private InternalDeployer                         internalDeployer;
  @Mock
  private RepositoryConfiguration                  repoConfig;
  
  private DistributionDeploymentRequestHandlerTaskHelper helper;
  
  @Before
  public void setUp() throws Exception {
    super.doSetUp();
    internalDeployer = mock(InternalDeployer.class);
    
    Endpoint ep = new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class));
    
    dist1 = new RepoDistribution("dist1", "1.0");
    dist2 = new RepoDistribution("dist2", "1.0");
    
    request1  = new DistributionDeploymentRequest(ep);
    request1.addDistribution(dist1);
    
    request2  = new DistributionDeploymentRequest(ep);
    request2.addDistribution(dist2);
    
    requests = new ArrayList<DistributionDeploymentRequest>();
    requests.add(request1);
    requests.add(request2);
    
    helper = new DistributionDeploymentRequestHandlerTaskHelper(repoConfig, taskContext, requests);
    
    ExecConfig conf1 = new ExecConfig();
    ProcessDef def1 = conf1.createProcess();
    def1.setDist("dist1");
    def1.setVersion("1.0");

    ExecConfig conf2 = new ExecConfig();
    ProcessDef def2 = conf2.createProcess();
    def2.setDist("dist2");
    def2.setVersion("1.0");
    
    execConfigs = new ArrayList<ExecConfig>();
    execConfigs.add(conf1);
    execConfigs.add(conf2);
    
    when(processor.getExecConfigs(any(ExecConfigCriteria.class))).thenReturn(execConfigs);
    when(serverContext.lookup(eq(InternalDeployer.class))).thenReturn(internalDeployer);
    when(internalDeployer.getDistributionFile(anyString(), anyString())).thenReturn(new File("test"));
  }
  
  @Test
  public void testExecuteWithMultipleDistributions() throws Throwable {
    CompositeTask task = new CompositeTask();
    helper.addTo(task);
  
    assertEquals(4, task.getChildTasks().size());
  }
  
  @Test
  public void testExecuteWithSingleDistribution() throws Throwable {
    
    requests.remove(requests.size() - 1);

    CompositeTask task = new CompositeTask();
    helper.addTo(task);
    
    assertEquals(2, task.getChildTasks().size());
  } 

  @Test
  public void testExecuteEmptyDistributions() throws Throwable {
    requests.clear();
    CompositeTask task = new CompositeTask();
    helper.addTo(task);
    assertEquals(0, task.getChildTasks().size());

  }


}
