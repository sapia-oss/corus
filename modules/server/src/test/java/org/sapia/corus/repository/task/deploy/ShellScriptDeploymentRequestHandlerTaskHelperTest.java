package org.sapia.corus.repository.task.deploy;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptManager;
import org.sapia.corus.client.services.repository.FileDeploymentRequest;
import org.sapia.corus.client.services.repository.ShellScriptDeploymentRequest;
import org.sapia.corus.deployer.InternalFileManager;
import org.sapia.corus.deployer.InternalShellScriptManager;
import org.sapia.corus.repository.task.AbstractRepoTaskTest;
import org.sapia.corus.taskmanager.util.CompositeTask;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class ShellScriptDeploymentRequestHandlerTaskHelperTest extends AbstractRepoTaskTest {
  
  private ShellScriptDeploymentRequestHandlerTaskHelper helper;
  private List<ShellScript>                      scripts;
  private List<ShellScriptDeploymentRequest>     requests;
  @Mock
  private Endpoint                               endpoint1;

  @Mock
  private Endpoint                               endpoint2;
  
  @Mock
  private InternalShellScriptManager             shellScriptMan;
  
  @Mock
  private File                                   fileToReturn;
  
  @Before
  public void setUp() throws FileNotFoundException {
    super.doSetUp();
    
    ShellScript script1 = new ShellScript("alias1", "file1", "desc1");
    ShellScript script2 = new ShellScript("alias2", "file2", "desc2");
    
    scripts = Collects.arrayToList(script1, script2);
    
    requests = new ArrayList<ShellScriptDeploymentRequest>();
    ShellScriptDeploymentRequest request1 = new ShellScriptDeploymentRequest(endpoint1, scripts);
    ShellScriptDeploymentRequest request2 = new ShellScriptDeploymentRequest(endpoint2, scripts);
    
    requests.add(request1);
    requests.add(request2);
    
    helper = new ShellScriptDeploymentRequestHandlerTaskHelper(repoConfig, taskContext, requests);
    
    when(super.serverContext.lookup(InternalShellScriptManager.class)).thenReturn(shellScriptMan);
    when(shellScriptMan.getScriptFile(any(ShellScript.class))).thenReturn(fileToReturn);
  }

  @Test
  public void testExecuteWithMultipleFiles() {
    CompositeTask task = new CompositeTask();
    helper.addTo(task);
    assertEquals(2, task.getChildTasks().size());
  }
  
  @Test
  public void testExecuteWithSingleFile() {
    scripts.remove(0);
    CompositeTask task = new CompositeTask();
    helper.addTo(task);
    assertEquals(1, task.getChildTasks().size());
  }
  
  
  @Test
  public void testExecuteWithFileNotFound() throws Exception {
    when(shellScriptMan.getScriptFile(any(ShellScript.class))).thenThrow(new FileNotFoundException("File not found"));
    CompositeTask task = new CompositeTask();
    helper.addTo(task);
    assertEquals(0, task.getChildTasks().size());
  }  
  
  @Test
  public void testGetFileTargets() {
    Map<ShellScript, Set<Endpoint>> targets = helper.getScriptTargets(taskContext, requests);
    for (Map.Entry<ShellScript, Set<Endpoint>> entry : targets.entrySet()) {
      assertTrue(entry.getValue().contains(requests.get(0).getEndpoint()));
      assertTrue(entry.getValue().contains(requests.get(1).getEndpoint()));      
    }
    
  }

}
