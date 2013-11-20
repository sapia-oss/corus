package org.sapia.corus.repository.task;

import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.repository.DistributionDeploymentRequest;
import org.sapia.corus.client.services.repository.DistributionListResponse;
import org.sapia.corus.client.services.repository.RepoDistribution;
import org.sapia.corus.taskmanager.util.RunnableTask;
import org.sapia.ubik.mcast.EventChannel;

/**
 * Task that handles {@link DistributionListResponse}ss.
 * 
 * @author yduchesne
 * 
 */
public class DistributionListResponseHandlerTask extends RunnableTask {

  private DistributionListResponse distsRes;

  /**
   * @param distsRes
   *          the {@link DistributionListResponse} to handle.
   */
  public DistributionListResponseHandlerTask(DistributionListResponse distsRes) {
    this.distsRes = distsRes;
  }

  @Override
  public void run() {
    ClusterManager cluster = context().getServerContext().getServices().getClusterManager();
    Deployer deployer = context().getServerContext().getServices().getDeployer();
    Set<RepoDistribution> toReceive = new HashSet<RepoDistribution>();
    context().debug(String.format("Got distribution list (%s) from %s", distsRes.getDistributions(), distsRes.getEndpoint()));
    for (RepoDistribution dist : distsRes.getDistributions()) {
      try {
        deployer.getDistribution(DistributionCriteria.builder().name(dist.getName()).version(dist.getVersion()).build());
      } catch (DistributionNotFoundException e) {
        context().info(String.format("Will be requesting distribution %s", dist));
        toReceive.add(dist);
      }
    }

    context().debug("Sending deployment request");
    EventChannel channel = cluster.getEventChannel();
    DistributionDeploymentRequest request = new DistributionDeploymentRequest(context().getServerContext().getCorusHost().getEndpoint());
    request.addDistributions(toReceive);
    try {
      channel.dispatch(distsRes.getEndpoint().getChannelAddress(), DistributionDeploymentRequest.EVENT_TYPE, request);
    } catch (Exception e) {
      context().error(String.format("Could not send distribution list to %s", distsRes.getEndpoint()), e);
    }
  }
}
