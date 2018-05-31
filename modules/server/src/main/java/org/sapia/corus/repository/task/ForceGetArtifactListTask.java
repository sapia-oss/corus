package org.sapia.corus.repository.task;

import java.util.Set;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.repository.ArtifactListRequest;
import org.sapia.corus.repository.PullProcessState;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.util.RunnableTask;
import org.sapia.ubik.mcast.EventChannel;

/**
 * This task sends a {@link ArtifactListRequest} to Corus repository server
 * nodes.
 * 
 * @author yduchesne
 * 
 */
public class ForceGetArtifactListTask extends RunnableTask {

  private OptionalValue<Endpoint>          sourceHostEndpoint;
  private PullProcessState                 state;
  private RepositoryTaskCompletionCallback completionCallback;
  
  public ForceGetArtifactListTask(
      OptionalValue<Endpoint> sourceHostEndpoint, 
      PullProcessState state,
      RepositoryTaskCompletionCallback completionCallback) {
    this.sourceHostEndpoint = sourceHostEndpoint;
    this.state              = state;
    this.completionCallback = completionCallback;
  }

  @Override
  public void run() {
    state.acquireLock();
    
    try {
      state.reset();
      
      Set<CorusHost> hosts = context().getServerContext().getServices().getClusterManager().getHosts();
      
      if (hosts.isEmpty()) {
        context().debug("Host list is empty - hosts probably not discovered yet");
      } else {
        for (CorusHost h : hosts) {
          if (h.getRepoRole() == RepoRole.SERVER || isSource(h)) {
            if (state.addContactedRepoServer(h)) {
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
        }
      }
      
    } finally {
      state.releaseLock();
    }
  }
  
  private boolean isSource(CorusHost host) {
    if (sourceHostEndpoint.isSet()) {
      return sourceHostEndpoint.get().getServerAddress().equals(host.getEndpoint().getServerAddress());
    }
    return false;
    
  }
  
  @Override
  protected void onMaxExecutionReached(TaskExecutionContext ctx) throws Throwable {
    completionCallback.taskCompleted();
  }

}
