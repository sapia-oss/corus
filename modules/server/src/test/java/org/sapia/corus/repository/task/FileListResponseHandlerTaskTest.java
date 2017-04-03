package org.sapia.corus.repository.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.deployer.FileManager;
import org.sapia.corus.client.services.repository.FileDeploymentRequest;
import org.sapia.corus.client.services.repository.FileListResponse;
import org.sapia.corus.repository.PullProcessState;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class FileListResponseHandlerTaskTest extends AbstractRepoTaskTest {
  
  private FileListResponseHandlerTask task;
  private PullProcessState            pullProcessState;
  private FileListResponse            response;
  private List<FileInfo>              files;
  private Endpoint                    serverEndpoint;
  
  @Mock
  private FileManager                 fileMan;
  
  @Before
  public void setUp() {
    super.doSetUp();
    
    files = Collects.arrayToList(
        new FileInfo("f1", 1, new Date()),
        new FileInfo("f2", 1, new Date())
    );
    
    serverEndpoint = new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class));
    response = new FileListResponse(serverEndpoint, files);
    
    pullProcessState = new PullProcessState();
    task     = new FileListResponseHandlerTask(response, pullProcessState);
    
    when(super.serviceContext.getFileManager()).thenReturn(fileMan);
  }

  @Test
  public void testExecuteWithDifferentFileList() throws Throwable {
    final FileInfo f3 = new FileInfo("f3", 1, new Date());
    final FileInfo f4 = new FileInfo("f4", 1, new Date());
    when(fileMan.getFiles()).thenReturn(Collects.arrayToList(f3, f4));
    
    task.execute(taskContext, null);
    
    verify(super.eventChannel).dispatch(any(ServerAddress.class), anyString(), argThat(new ArgumentMatcher<FileDeploymentRequest>() {
      @Override
      public boolean matches(Object argument) {
        FileDeploymentRequest req = (FileDeploymentRequest) argument;
        return req.getFiles().size() == 2 && req.getFiles().containsAll(files);
      }
    }));
    assertThat(pullProcessState.getDiscoveredFilesFromHost(serverEndpoint.getChannelAddress())).containsOnly(files.get(0), files.get(1));
  }

  @Test
  public void testExecuteFileAlreadyDiscoveredFromAnotherHost() throws Throwable {
    final FileInfo f3 = new FileInfo("f3", 1, new Date());
    final FileInfo f4 = new FileInfo("f4", 1, new Date());
    when(fileMan.getFiles()).thenReturn(Collects.arrayToList(f3, f4));
    pullProcessState.addDiscoveredFileFromHostIfAbsent(files.get(0), mock(ServerAddress.class));
    
    task.execute(taskContext, null);
    
    verify(super.eventChannel).dispatch(any(ServerAddress.class), anyString(), argThat(new ArgumentMatcher<FileDeploymentRequest>() {
      @Override
      public boolean matches(Object argument) {
        FileDeploymentRequest req = (FileDeploymentRequest) argument;
        return req.getFiles().size() == 1 && req.getFiles().contains(files.get(1));
      }
    }));
    assertThat(pullProcessState.getDiscoveredFilesFromHost(serverEndpoint.getChannelAddress())).containsOnly(files.get(1));
  }
  
  @Test
  public void testExecuteWithIntersectingFileList() throws Throwable {
    final FileInfo f2 = new FileInfo("f2", 1, new Date());
    final FileInfo f3 = new FileInfo("f3", 1, new Date());
    when(fileMan.getFiles()).thenReturn(Collects.arrayToList(f2, f3));
    
    task.execute(taskContext, null);
    
    verify(super.eventChannel).dispatch(any(ServerAddress.class), anyString(), argThat(new ArgumentMatcher<FileDeploymentRequest>() {
      @Override
      public boolean matches(Object argument) {
        FileDeploymentRequest req = (FileDeploymentRequest) argument;
        return req.getFiles().size() == 1 && req.getFiles().contains(new FileInfo("f1", 1, new Date()));
      }
    }));
    assertThat(pullProcessState.getDiscoveredFilesFromHost(serverEndpoint.getChannelAddress())).containsOnly(files.get(0));
  }  
  
  @Test
  public void testExecuteWithSameFileList() throws Throwable {
    final FileInfo f1 = new FileInfo("f1", 1, new Date());
    final FileInfo f2 = new FileInfo("f2", 1, new Date());
    when(fileMan.getFiles()).thenReturn(Collects.arrayToList(f1, f2));
    
    task.execute(taskContext, null);
    
    verify(super.eventChannel, never()).dispatch(any(ServerAddress.class), anyString(), any(FileDeploymentRequest.class));
    assertThat(pullProcessState.getDiscoveredFilesFromHost(serverEndpoint.getChannelAddress())).isEmpty();
  }

}
