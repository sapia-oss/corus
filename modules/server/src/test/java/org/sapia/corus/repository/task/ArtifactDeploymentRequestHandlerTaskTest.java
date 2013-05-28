package org.sapia.corus.repository.task;


import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.repository.ArtifactDeploymentRequest;
import org.sapia.corus.client.services.repository.DistributionDeploymentRequest;
import org.sapia.corus.client.services.repository.FileDeploymentRequest;
import org.sapia.corus.client.services.repository.ShellScriptDeploymentRequest;
import org.sapia.corus.repository.task.deploy.ArtifactDeploymentHandlerTaskHelper;
import org.sapia.corus.taskmanager.util.CompositeTask;
import org.sapia.corus.util.Queue;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactDeploymentRequestHandlerTaskTest extends AbstractRepoTaskTest {
  
  @Mock
  private Queue<ArtifactDeploymentRequest>         requests;
  
  private List<ArtifactDeploymentRequest>          requestList;
  
  private ArtifactDeploymentRequestHandlerTask     task;
  
  @Mock
  private TestDeploymentTaskHelper                 helper;
  
  @Before
  public void setUp() throws Exception {
    super.doSetUp(); 

    requestList = new ArrayList<ArtifactDeploymentRequest>();
    requestList.add(new DistributionDeploymentRequest(mock(Endpoint.class)));
    
    task = new ArtifactDeploymentRequestHandlerTask(repoConfig, requests) {
      
      @Override
      ArtifactDeploymentHandlerTaskHelper getDistributionHelper(
          List<DistributionDeploymentRequest> requests) {
        return helper;
      }
      
      @Override
      ArtifactDeploymentHandlerTaskHelper getFileHelper(
          List<FileDeploymentRequest> requests) {
        return helper;
      }
      
      @Override
      ArtifactDeploymentHandlerTaskHelper getShellScriptHelper(
          List<ShellScriptDeploymentRequest> requests) {
        return helper;
      }
    };

    when(requests.removeAll()).thenReturn(requestList);
  }
  
  @Test
  public void testExecuteWithRequests() throws Throwable {
    this.task.execute(taskContext, null);
    verify(helper, times(3)).addTo(any(CompositeTask.class));
  }
  
  @Test
  public void testExecuteWithEmptyRequests() throws Throwable {
    requestList.clear();
    this.task.execute(taskContext, null);
    verify(helper, never()).addTo(any(CompositeTask.class));
  }  

  private class TestDeploymentTaskHelper extends ArtifactDeploymentHandlerTaskHelper {
    
    public TestDeploymentTaskHelper() {
      super(repoConfig, taskContext);
    }
    
    @Override
    public void addTo(CompositeTask tasks) {
    }
  }  
}
