package org.sapia.corus.repository.task;

import java.util.Collection;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.repository.ArtifactDeploymentRequest;
import org.sapia.corus.client.services.repository.ConfigDeploymentRequest;
import org.sapia.corus.client.services.repository.DistributionDeploymentRequest;
import org.sapia.corus.client.services.repository.DistributionListResponse;
import org.sapia.corus.client.services.repository.RepoDistribution;
import org.sapia.corus.repository.PullProcessState;
import org.sapia.corus.taskmanager.util.RunnableTask;

/**
 * Task that handles {@link DistributionListResponse}s.
 * 
 * @author yduchesne
 * @author jcdesrochers
 */
public class DistributionListResponseHandlerTask extends RunnableTask {

  private DistributionListResponse distsRes;
  private PullProcessState         state;

  /**
   * @param distsRes the {@link DistributionListResponse} to handle.
   * @param state    the state of the pull repository process.
   * @param force    the 'force' flag.
   */
  public DistributionListResponseHandlerTask(DistributionListResponse distsRes, PullProcessState state) {
    this.distsRes = distsRes;
    this.state    = state;
  }

  @Override
  public void run() {
    state.acquireLock();
    
    try {
      context().info("Processing distribution list response from host " + distsRes.getEndpoint());
      Collection<RepoDistribution> discoveredDistributions = doProcessResponseAndUpdatePullState();
      
      if (discoveredDistributions.isEmpty()) {
        context().debug("No distribution to request from host " + distsRes.getEndpoint() + ". Will request configuration");
        doSendConfigDeploymentRequest();
      } else {
        doSendDistributionDeploymentRequest(discoveredDistributions);
      }
    } finally {
      state.releaseLock();
    }
  }
  
  private Collection<RepoDistribution> doProcessResponseAndUpdatePullState() {
    Deployer deployer = context().getServerContext().getServices().getDeployer();
    Endpoint repoServerEndpoint = distsRes.getEndpoint();

    for (RepoDistribution dist : distsRes.getDistributions()) {
      try {
        deployer.getDistribution(DistributionCriteria.builder().name(dist.getName()).version(dist.getVersion()).build());
      } catch (DistributionNotFoundException e) {
        if (state.addDiscoveredDistributionFromHostIfAbsent(dist, repoServerEndpoint.getChannelAddress())) {
          context().info(String.format("Found new distribution %s from host %s", dist, repoServerEndpoint));
        } else {
          context().info(String.format("Distribution %s already discovered form another host", dist));
        }
      }
    }
    
    return state.getDiscoveredDistributionsFromHost(repoServerEndpoint.getChannelAddress());
  }
  
  private void doSendConfigDeploymentRequest() {
    ConfigDeploymentRequest confReq = new ConfigDeploymentRequest(context().getServerContext().getCorusHost().getEndpoint());
    confReq.setForce(distsRes.isForce());   
    doSendRequest(confReq, ConfigDeploymentRequest.EVENT_TYPE);
  }

  private void doSendDistributionDeploymentRequest(Collection<RepoDistribution> distributions) {
    DistributionDeploymentRequest distReq = new DistributionDeploymentRequest(context().getServerContext().getCorusHost().getEndpoint());
    distReq.setForce(distsRes.isForce());
    distReq.addDistributions(distributions);  
    doSendRequest(distReq, DistributionDeploymentRequest.EVENT_TYPE);
  }
  
  private void doSendRequest(ArtifactDeploymentRequest request, String eventType) {
    try {
      ClusterManager cluster            = context().getServerContext().getServices().getClusterManager();
      Endpoint       repoServerEndpoint = distsRes.getEndpoint();
      context().info("Sending deployment request to host " + repoServerEndpoint);
      cluster.getEventChannel().dispatch(repoServerEndpoint.getChannelAddress(), eventType, request).get();
    } catch (Exception e) {
      context().error(String.format("Could not send deployment request to %s", distsRes.getEndpoint()), e);
    }
  }
}
