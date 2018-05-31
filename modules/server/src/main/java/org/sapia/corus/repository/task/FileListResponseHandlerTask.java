package org.sapia.corus.repository.task;

import java.util.Collection;
import java.util.List;

import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.deployer.FileManager;
import org.sapia.corus.client.services.repository.FileDeploymentRequest;
import org.sapia.corus.client.services.repository.FileListResponse;
import org.sapia.corus.repository.PullProcessState;
import org.sapia.corus.taskmanager.util.RunnableTask;

/**
 * Task that handles {@link FileListResponse}s.
 * 
 * @author yduchesne
 * @author jcdesrochers
 */
public class FileListResponseHandlerTask extends RunnableTask {

  private FileListResponse filesRes;
  private PullProcessState state;


  /**
   * @param filesRes The {@link FileListResponse} to handle.
   * @param state the state of the pull repository process.
   */
  public FileListResponseHandlerTask(FileListResponse filesRes, PullProcessState state) {
    this.filesRes = filesRes;
    this.state = state;
  }

  @Override
  public void run() {
    state.acquireLock();
    
    try {
      context().info("Processing file list response from host " + filesRes.getEndpoint());
      Collection<FileInfo> discoveredFiles = doProcessResponseAndUpdatePullState();
      
      if (discoveredFiles.isEmpty()) {
        context().debug("No file to request from host " + filesRes.getEndpoint());
      } else {
        doSendFileDeploymentRequest(discoveredFiles);
      }
      
    } finally {
      state.releaseLock();
    }
  }
  
  private Collection<FileInfo> doProcessResponseAndUpdatePullState() {
    FileManager manager = context().getServerContext().getServices().getFileManager();
    Endpoint repoServerEndpoint = filesRes.getEndpoint();
    
    List<FileInfo> existingFiles = manager.getFiles();
    for (FileInfo discoveredFile: filesRes.getFiles()) {
      if (!existingFiles.contains(discoveredFile)) {
        if (state.addDiscoveredFileFromHostIfAbsent(discoveredFile, repoServerEndpoint.getChannelAddress())) {
          context().info(String.format("Found new file %s from host %s", discoveredFile, repoServerEndpoint));
        } else {
          context().info(String.format("File %s already discovered from another host", discoveredFile));
        }
      }
    }
    
    return state.getDiscoveredFilesFromHost(repoServerEndpoint.getChannelAddress());
  }
  
  
  private void doSendFileDeploymentRequest(Collection<FileInfo> files) {
    ClusterManager cluster = context().getServerContext().getServices().getClusterManager();
    Endpoint repoServerEndpoint = filesRes.getEndpoint();

    FileDeploymentRequest request = new FileDeploymentRequest(context().getServerContext().getCorusHost().getEndpoint(), files);
    request.setForce(filesRes.isForce());
    try {
      context().info("Sending file deployment request to host " + repoServerEndpoint);
      cluster.getEventChannel().dispatch(repoServerEndpoint.getChannelAddress(), FileDeploymentRequest.EVENT_TYPE, request).get();
    } catch (Exception e) {
      context().error(String.format("Could not send file deployment request to %s", repoServerEndpoint), e);
    }
  }
  
}
