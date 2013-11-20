package org.sapia.corus.repository.task;

import java.util.List;

import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.deployer.FileManager;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptManager;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.repository.ArtifactListRequest;
import org.sapia.corus.client.services.repository.DistributionListResponse;
import org.sapia.corus.client.services.repository.FileListResponse;
import org.sapia.corus.client.services.repository.RepoDistribution;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.client.services.repository.ShellScriptListResponse;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.util.RunnableTask;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collections2;
import org.sapia.ubik.util.Function;

/**
 * Internally removes {@link ArtifactListRequest}s from the passed in queue,
 * sending back the corresponding response {@link DistributionListResponse} for
 * each such request.
 * 
 * @author yduchesne
 * 
 */
public class ArtifactListRequestHandlerTask extends RunnableTask {

  private org.sapia.corus.util.Queue<ArtifactListRequest> requestQueue;
  private RepositoryConfiguration config;

  public ArtifactListRequestHandlerTask(RepositoryConfiguration config, org.sapia.corus.util.Queue<ArtifactListRequest> requests) {
    this.config = config;
    this.requestQueue = requests;
  }

  @Override
  public void run() {
    for (ArtifactListRequest req : requestQueue.removeAll()) {
      context().info("Processing " + req);
      processRequest(req, context());
    }
  }

  // --------------------------------------------------------------------------
  // Package visibility for testing purposes

  void processRequest(ArtifactListRequest request, TaskExecutionContext ctx) {
    doSendDistributionListResponse(request, ctx);
    if (config.isPushScriptsEnabled()) {
      ctx.debug("Sending script list to " + request.getEndpoint());
      doSendScriptListResponse(request, ctx);
    } else {
      ctx.debug("Scripts push disabled, will not send script list to " + request.getEndpoint());
    }
    if (config.isPushFilesEnabled()) {
      ctx.debug("Sending file list to " + request.getEndpoint());
      doSendFileListResponse(request, ctx);
    } else {
      ctx.debug("File push disabled, will not send file list to " + request.getEndpoint());
    }
  }

  void doSendDistributionListResponse(ArtifactListRequest request, TaskExecutionContext ctx) {
    ServerAddress addr = ctx.getServerContext().getCorusHost().getEndpoint().getServerAddress();
    Deployer deployer = ctx.getServerContext().getServices().getDeployer();
    ClusterManager cluster = ctx.getServerContext().getServices().getClusterManager();

    List<RepoDistribution> dists = Collections2.convertAsList(deployer.getDistributions(DistributionCriteria.builder().all()),
        new Function<RepoDistribution, Distribution>() {
          @Override
          public RepoDistribution call(Distribution dist) {
            return new RepoDistribution(dist.getName(), dist.getVersion());
          }
        });

    DistributionListResponse response = new DistributionListResponse(ctx.getServerContext().getCorusHost().getEndpoint());
    response.addDistributions(dists);
    try {
      cluster.getEventChannel().dispatch(request.getEndpoint().getChannelAddress(), DistributionListResponse.EVENT_TYPE, response);
    } catch (Exception e) {
      ctx.error(String.format("Could not send distribution list to %s", addr), e);
    }
  }

  void doSendScriptListResponse(ArtifactListRequest request, TaskExecutionContext ctx) {
    ServerAddress addr = ctx.getServerContext().getCorusHost().getEndpoint().getServerAddress();
    ShellScriptManager manager = ctx.getServerContext().getServices().getScriptManager();
    ClusterManager cluster = ctx.getServerContext().getServices().getClusterManager();

    List<ShellScript> scripts = manager.getScripts();
    ShellScriptListResponse response = new ShellScriptListResponse(ctx.getServerContext().getCorusHost().getEndpoint(), scripts);

    try {
      cluster.getEventChannel().dispatch(request.getEndpoint().getChannelAddress(), ShellScriptListResponse.EVENT_TYPE, response);
    } catch (Exception e) {
      ctx.error(String.format("Could not send script list to %s", addr), e);
    }
  }

  void doSendFileListResponse(ArtifactListRequest request, TaskExecutionContext ctx) {
    ServerAddress addr = ctx.getServerContext().getCorusHost().getEndpoint().getServerAddress();
    FileManager manager = ctx.getServerContext().getServices().getFileManager();
    ClusterManager cluster = ctx.getServerContext().getServices().getClusterManager();

    List<FileInfo> files = manager.getFiles();
    FileListResponse response = new FileListResponse(ctx.getServerContext().getCorusHost().getEndpoint(), files);

    try {
      cluster.getEventChannel().dispatch(request.getEndpoint().getChannelAddress(), FileListResponse.EVENT_TYPE, response);
    } catch (Exception e) {
      ctx.error(String.format("Could not send files list to %s", addr), e);
    }
  }
}
