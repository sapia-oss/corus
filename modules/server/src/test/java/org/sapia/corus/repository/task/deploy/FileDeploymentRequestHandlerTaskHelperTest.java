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
import org.sapia.corus.client.services.repository.FileDeploymentRequest;
import org.sapia.corus.deployer.InternalFileManager;
import org.sapia.corus.repository.task.AbstractRepoTaskTest;
import org.sapia.corus.taskmanager.util.CompositeTask;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class FileDeploymentRequestHandlerTaskHelperTest extends AbstractRepoTaskTest {
  
  private FileDeploymentRequestHandlerTaskHelper helper;
  private List<FileInfo>                         files;
  private List<FileDeploymentRequest>            requests;
  @Mock
  private Endpoint                               endpoint1;

  @Mock
  private Endpoint                               endpoint2;
  
  @Mock
  private InternalFileManager                    fileMan;
  
  @Mock
  private File                                   fileToReturn;
  
  @Before
  public void setUp() throws FileNotFoundException {
    super.doSetUp();
    
    FileInfo file1 = new FileInfo("file1", 100, new Date());
    FileInfo file2 = new FileInfo("file2", 100, new Date());
    
    files = Collects.arrayToList(file1, file2);
    
    requests = new ArrayList<FileDeploymentRequest>();
    FileDeploymentRequest request1 = new FileDeploymentRequest(endpoint1, files);
    FileDeploymentRequest request2 = new FileDeploymentRequest(endpoint2, files);
    
    requests.add(request1);
    requests.add(request2);
    
    helper = new FileDeploymentRequestHandlerTaskHelper(repoConfig, taskContext, requests);
    
    when(super.serverContext.lookup(InternalFileManager.class)).thenReturn(fileMan);
    when(fileMan.getFile(any(FileInfo.class))).thenReturn(fileToReturn);
  }

  @Test
  public void testExecuteWithMultipleFiles() {
    CompositeTask task = new CompositeTask();
    helper.addTo(task);
    assertEquals(2, task.getChildTasks().size());
  }
  
  @Test
  public void testExecuteWithSingleFile() {
    files.remove(0);
    CompositeTask task = new CompositeTask();
    helper.addTo(task);
    assertEquals(1, task.getChildTasks().size());
  }
  
  
  @Test
  public void testExecuteWithFileNotFound() throws Exception {
    when(fileMan.getFile(any(FileInfo.class))).thenThrow(new FileNotFoundException("File not found"));
    CompositeTask task = new CompositeTask();
    helper.addTo(task);
    assertEquals(0, task.getChildTasks().size());
  }  
  
  @Test
  public void testGetFileTargets() {
    Map<FileInfo, Set<Endpoint>> targets = helper.getFileTargets(taskContext, requests);
    for (Map.Entry<FileInfo, Set<Endpoint>> entry : targets.entrySet()) {
      assertTrue(entry.getValue().contains(requests.get(0).getEndpoint()));
      assertTrue(entry.getValue().contains(requests.get(1).getEndpoint()));      
    }
    
  }

}
