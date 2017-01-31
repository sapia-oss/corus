package org.sapia.corus.repository.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.deployer.FileManager;
import org.sapia.corus.client.services.repository.FileDeploymentRequest;
import org.sapia.corus.client.services.repository.FileListResponse;
import org.sapia.corus.taskmanager.util.RunnableTask;
import org.sapia.ubik.mcast.EventChannel;

/**
 * Task that handles {@link FileListResponse}s.
 * 
 * @author yduchesne
 * 
 */
public class FileListResponseHandlerTask extends RunnableTask {

  private FileListResponse filesRes;

  /**
   * @param filesRes
   *          the {@link FileListResponse} to handle.
   */
  public FileListResponseHandlerTask(FileListResponse filesRes) {
    this.filesRes = filesRes;
  }

  @Override
  public void run() {
    ClusterManager cluster = context().getServerContext().getServices().getClusterManager();
    FileManager manager = context().getServerContext().getServices().getFileManager();

    Set<FileInfo> toReceive = new HashSet<FileInfo>();

    toReceive.addAll(filesRes.getFiles());
    toReceive.removeAll(manager.getFiles());

    context().debug(String.format("Got file list (%s) from %s", filesRes.getFiles(), filesRes.getEndpoint()));

    if (!toReceive.isEmpty()) {
      context().debug("Sending deployment request");
      EventChannel channel = cluster.getEventChannel();
      FileDeploymentRequest request = new FileDeploymentRequest(context().getServerContext().getCorusHost().getEndpoint(), new ArrayList<FileInfo>(
          toReceive));

      try {
        channel.dispatch(filesRes.getEndpoint().getChannelAddress(), FileDeploymentRequest.EVENT_TYPE, request).get();
      } catch (Exception e) {
        context().error(String.format("Could not send file list to %s", filesRes.getEndpoint()), e);
      }
    } else {
      context().debug("No files to request from " + filesRes.getEndpoint());
    }
  }
}
