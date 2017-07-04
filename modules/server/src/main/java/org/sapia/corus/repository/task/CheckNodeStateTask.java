package org.sapia.corus.repository.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.services.ModuleState;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.repository.ArtifactListRequest;
import org.sapia.corus.client.services.repository.Repository;
import org.sapia.corus.repository.PullProcessState;
import org.sapia.corus.taskmanager.util.RunnableTask;
import org.sapia.ubik.mcast.EventChannel;

/**
 * This task will check if distributions are deployed to the current Corus node. If not,
 * it will attempt contacting repo servers. If no repo servers are found, it will
 * attempt to contact a random number of Corus peers.
 * 
 * @author yduchesne
 *
 */
public class CheckNodeStateTask extends RunnableTask {
 
  private Reference<ModuleState> moduleState;
  private PullProcessState       pullState;
  private int                    maxRandomHosts;
  private boolean                checkRandomHostsEnabled;
  
  /**
   * @param moduleState a {@link Reference} holding the {@link Repository}'s {@link ModuleState}.
   * @param pullState the shared {@link PullProcessState}.
   * @param maxRandomHosts the maximum number of random peers to contact, if no
   *        repo servers are found.
   * @param checkRandomHostsEnabled <code>true</code> if checking/selecting and eventually receiving a
   *        a copy of the state of random hosts should be enabled or not.
   */
  public CheckNodeStateTask(
      Reference<ModuleState> moduleState, 
      PullProcessState pullState, 
      int maxRandomHosts, 
      boolean checkRandomHostsEnabled) {
    this.moduleState    = moduleState;
    this.pullState      = pullState;
    this.maxRandomHosts = maxRandomHosts;
    this.checkRandomHostsEnabled = checkRandomHostsEnabled;
  }
  
  public int getMaxRandomHosts() {
    return maxRandomHosts;
  }
  
  public boolean isCheckRandomHostsEnabled() {
    return checkRandomHostsEnabled;
  }

  @Override
  public void run() {
    if (moduleState.setIf(ModuleState.BUSY, ModuleState.IDLE)) {
      doRun();
    } else {
      context().debug("Repository in busy state. Bypassing state checking for now");
    }
 
  }
  
  private void doRun() {
    context().debug("Checking if this node has distributions");
    
    Collection<Distribution> dists = context()
        .getServerContext().getServices().getDeployer()
        .getDistributions(DistributionCriteria.builder().all());
    
    if (dists.isEmpty()) {
      context().warn("This node has no distribution. Synchronization with repo servers (or randomly selected peers) will be attempted");
      doCheck(dists);
    } else {
      context().debug("Node has distribution(s): nothing to do");
    }
  }
  
  private void doCheck(Collection<Distribution> dists) {
    pullState.acquireLock();
    
    try {
      pullState.reset();
      
      Set<CorusHost> allHosts = context().getServerContext().getServices().getClusterManager().getHosts();
      
      if (allHosts.isEmpty()) {
        context().warn("This node has no peer, cannot probe other nodes for state");
        return;
      }
      
      Set<CorusHost> targetHosts = allHosts.stream()
          .filter(h -> h.getRepoRole() == RepoRole.SERVER)
          .collect(Collectors.toSet());
      
      if (targetHosts.isEmpty()) {
        if (!checkRandomHostsEnabled) {
          context().warn("No repo server nodes could be found. Checking random hosts is disabled"
              + " - sync will happen only if repo server nodes appear");
        } else {
          context().warn("No repo server nodes could be found. Will try to contact random peers");
          List<CorusHost> randomHosts = new ArrayList<>(allHosts);
          Collections.shuffle(randomHosts);
          targetHosts = new HashSet<>();
          for (int i = 0; i < maxRandomHosts && i < randomHosts.size(); i++) {
            targetHosts.add(randomHosts.get(i));
          }
        }
      }
      
      for (CorusHost h : targetHosts) {
        if (pullState.addContactedRepoServer(h)) {
          context().debug("Will pull from: " + h);
          try {
            EventChannel        channel = context().getServerContext().getServices().getClusterManager().getEventChannel();
            ArtifactListRequest request = new ArtifactListRequest(context().getServerContext().getCorusHost().getEndpoint())
                .setForce(true);
            channel.dispatch(h.getEndpoint().getChannelAddress(), ArtifactListRequest.EVENT_TYPE, request);
          } catch (Exception e) {
            context().error("Could not dispatch distribution list request", e);
          }
        } else {
          context().debug("Corus host already contacted, ignoring: " + h);
        }
      }
    } finally {
      pullState.releaseLock();
    }
  }

}
