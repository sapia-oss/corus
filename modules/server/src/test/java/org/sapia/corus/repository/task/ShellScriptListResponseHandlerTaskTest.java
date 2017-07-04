package org.sapia.corus.repository.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptManager;
import org.sapia.corus.client.services.repository.ShellScriptDeploymentRequest;
import org.sapia.corus.client.services.repository.ShellScriptListResponse;
import org.sapia.corus.repository.PullProcessState;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class ShellScriptListResponseHandlerTaskTest extends AbstractRepoTaskTest {
  
  private ShellScriptListResponseHandlerTask task;
  private PullProcessState                   pullProcessState; 
  private ShellScriptListResponse            response;
  private List<ShellScript>                  scripts;
  private Endpoint                           serverEndpoint;
  
  @Mock
  private ShellScriptManager                 scriptMan;
  
  @Before
  public void setUp() {
    super.doSetUp();
    
    scripts = Collects.arrayToList(
        new ShellScript("f1", "f1", "f1"),
        new ShellScript("f2", "f2", "f2")
    );
    
    serverEndpoint = new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class));
    response = new ShellScriptListResponse(serverEndpoint, scripts);
    
    pullProcessState = new PullProcessState();
    task     = new ShellScriptListResponseHandlerTask(response, pullProcessState);
    
    when(super.serviceContext.getScriptManager()).thenReturn(scriptMan);
  }

  @Test
  public void testExecuteWithDifferentShellScriptList() throws Throwable {
    final ShellScript f3 = new ShellScript("f3", "f3", "f3");
    final ShellScript f4 = new ShellScript("f4", "f4", "f4");
    when(scriptMan.getScripts()).thenReturn(Collects.arrayToList(f3, f4));
    
    task.execute(taskContext, null);
    
    verify(super.eventChannel).dispatch(any(ServerAddress.class), anyString(), argThat(new ArgumentMatcher<ShellScriptDeploymentRequest>() {
      @Override
      public boolean matches(Object argument) {
        ShellScriptDeploymentRequest req = (ShellScriptDeploymentRequest) argument;
        return req.getScripts().size() == 2 && req.getScripts().containsAll(scripts);
      }
    }));
    assertThat(pullProcessState.getDiscoveredScriptsFromHost(serverEndpoint.getChannelAddress())).containsOnly(scripts.get(0), scripts.get(1));
  }
  
  @Test
  public void testExecuteWithForce() throws Throwable {
    response.setForce(true);
    final ShellScript f3 = new ShellScript("f3", "f3", "f3");
    final ShellScript f4 = new ShellScript("f4", "f4", "f4");
    when(scriptMan.getScripts()).thenReturn(Collects.arrayToList(f3, f4));
    
    task.execute(taskContext, null);
    
    verify(super.eventChannel).dispatch(any(ServerAddress.class), anyString(), argThat(new ArgumentMatcher<ShellScriptDeploymentRequest>() {
      @Override
      public boolean matches(Object argument) {
        ShellScriptDeploymentRequest req = (ShellScriptDeploymentRequest) argument;
        return req.isForce();
      }
    }));
    assertThat(pullProcessState.getDiscoveredScriptsFromHost(serverEndpoint.getChannelAddress())).containsOnly(scripts.get(0), scripts.get(1));
  }
  
  @Test
  public void testExecuteWithIntersectingShellScriptList() throws Throwable {
    final ShellScript f2 = new ShellScript("f2", "f2", "f2");
    final ShellScript f3 = new ShellScript("f3", "f3", "f3");
    when(scriptMan.getScripts()).thenReturn(Collects.arrayToList(f2, f3));
    
    task.execute(taskContext, null);
    
    verify(super.eventChannel).dispatch(any(ServerAddress.class), anyString(), argThat(new ArgumentMatcher<ShellScriptDeploymentRequest>() {
      @Override
      public boolean matches(Object argument) {
        ShellScriptDeploymentRequest req = (ShellScriptDeploymentRequest) argument;
        return req.getScripts().size() == 1 && req.getScripts().contains(new ShellScript("f1", "f1", "f1"));
      }
    }));
    assertThat(pullProcessState.getDiscoveredScriptsFromHost(serverEndpoint.getChannelAddress())).containsOnly(scripts.get(0));
  }  
  
  @Test
  public void testExecuteScriptAlreadyDiscoveredFromAnotherHost() throws Throwable {
    final ShellScript f3 = new ShellScript("f3", "f3", "f3");
    final ShellScript f4 = new ShellScript("f4", "f4", "f4");
    when(scriptMan.getScripts()).thenReturn(Collects.arrayToList(f3, f4));
    pullProcessState.addDiscoveredScriptFromHostIfAbsent(new ShellScript("f1", "f1", "f1"), mock(ServerAddress.class));
    
    task.execute(taskContext, null);
    
    verify(super.eventChannel).dispatch(any(ServerAddress.class), anyString(), argThat(new ArgumentMatcher<ShellScriptDeploymentRequest>() {
      @Override
      public boolean matches(Object argument) {
        ShellScriptDeploymentRequest req = (ShellScriptDeploymentRequest) argument;
        return req.getScripts().size() == 1 && req.getScripts().contains(new ShellScript("f2", "f2", "f2"));
      }
    }));
    assertThat(pullProcessState.getDiscoveredScriptsFromHost(serverEndpoint.getChannelAddress())).containsOnly(scripts.get(1));
  }
  
  @Test
  public void testExecuteWithSameFileList() throws Throwable {
    final ShellScript f1 = new ShellScript("f1", "f1", "f1");
    final ShellScript f2 = new ShellScript("f2", "f2", "f2");
    when(scriptMan.getScripts()).thenReturn(Collects.arrayToList(f1, f2));
    
    task.execute(taskContext, null);
    
    verify(super.eventChannel, never()).dispatch(any(ServerAddress.class), anyString(), any(ShellScriptDeploymentRequest.class));
    assertThat(pullProcessState.getDiscoveredScriptsFromHost(serverEndpoint.getChannelAddress())).isEmpty();
  }

}
