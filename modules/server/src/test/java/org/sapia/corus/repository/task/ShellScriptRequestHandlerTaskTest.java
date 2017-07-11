package org.sapia.corus.repository.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.cluster.ClusterNotification;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;


public class ShellScriptRequestHandlerTaskTest extends AbstractRepoTaskTest {
  
  private TestDeploymentRequestHandlerTask task;
  private List<Endpoint>                   targets;
  
  @Before
  public void setUp() throws Exception {
    super.doSetUp();
    Endpoint ep = new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class));
    targets     = Collects.arrayToList(ep);
    task = new TestDeploymentRequestHandlerTask(new File("test"), targets);
  }

  @Test
  public void testExecuteWithEmptyTargets() throws Throwable {
    targets.clear();
    task.execute(taskContext, null);
    assertFalse(task.deploy);
    verify(cluster, never()).dispatch(any(ClusterNotification.class));
  }

  @Test
  public void testExecuteWithTargets() throws Throwable {
    task.execute(taskContext, null);
    assertTrue(task.deploy);
  }
  
  // ==========================================================================
  
  static class TestDeploymentRequestHandlerTask extends ShellScriptRequestHandlerTask {
    
    boolean deploy;
    
    public TestDeploymentRequestHandlerTask(File file, List<Endpoint> targets) {
      super(file, new ShellScript("f1", "f1", "f1"), targets);
    }
    
    @Override
    void doDeploy() throws IOException {
      deploy = true;
    }
    
  }

}
