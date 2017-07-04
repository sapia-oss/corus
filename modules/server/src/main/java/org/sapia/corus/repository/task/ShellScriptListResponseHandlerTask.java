package org.sapia.corus.repository.task;

import java.util.Collection;
import java.util.List;

import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptManager;
import org.sapia.corus.client.services.repository.ShellScriptDeploymentRequest;
import org.sapia.corus.client.services.repository.ShellScriptListResponse;
import org.sapia.corus.repository.PullProcessState;
import org.sapia.corus.taskmanager.util.RunnableTask;

/**
 * Task that handles {@link ShellScriptListResponse}s.
 * 
 * @author yduchesne
 * @author jcdesrochers
 */
public class ShellScriptListResponseHandlerTask extends RunnableTask {

  private ShellScriptListResponse scriptsRes;
  private PullProcessState        state;

  /**
   * @param scriptsRes The {@link ShellScriptListResponse} to handle.
   * @param state the state of the pull repository process.
   */
  public ShellScriptListResponseHandlerTask(ShellScriptListResponse scriptsRes, PullProcessState state) {
    this.scriptsRes = scriptsRes;
    this.state = state;
  }

  @Override
  public void run() {
    state.acquireLock();
    try {
      context().info("Processing shell script list response from host " + scriptsRes.getEndpoint());
      Collection<ShellScript> discoveredScripts = doProcessResponseAndUpdatePullState();
      
      if (discoveredScripts.isEmpty()) {
        context().debug("No shell script to request from host " + scriptsRes.getEndpoint());
      } else {
        doSendShellScriptDeploymentRequest(discoveredScripts);
      }
      
    } finally {
      state.releaseLock();
    }
  }
  
  private Collection<ShellScript> doProcessResponseAndUpdatePullState() {
    ShellScriptManager manager = context().getServerContext().getServices().getScriptManager();
    Endpoint repoServerEndpoint = scriptsRes.getEndpoint();
    
    List<ShellScript> existingScripts = manager.getScripts();
    for (ShellScript discoveredScript: scriptsRes.getScripts()) {
      if (!existingScripts.contains(discoveredScript)) {
        if (state.addDiscoveredScriptFromHostIfAbsent(discoveredScript, repoServerEndpoint.getChannelAddress())) {
          context().info(String.format("Found new shell script %s from host %s", discoveredScript, repoServerEndpoint));
        } else {
          context().info(String.format("Shell script %s already discovered from another host", discoveredScript));
        }
      }
    }
    
    return state.getDiscoveredScriptsFromHost(repoServerEndpoint.getChannelAddress());
  }
  
  private void doSendShellScriptDeploymentRequest(Collection<ShellScript> scripts) {
    ClusterManager cluster = context().getServerContext().getServices().getClusterManager();
    Endpoint repoServerEndpoint = scriptsRes.getEndpoint();

    ShellScriptDeploymentRequest request = new ShellScriptDeploymentRequest(context().getServerContext().getCorusHost().getEndpoint(), scripts);
    request.setForce(scriptsRes.isForce());
    
    try {
      context().info("Sending shell script deployment request to host " + repoServerEndpoint);
      cluster.getEventChannel().dispatch(repoServerEndpoint.getChannelAddress(), ShellScriptDeploymentRequest.EVENT_TYPE, request).get();
    } catch (Exception e) {
      context().error(String.format("Could not send shell script deployment request to %s", repoServerEndpoint), e);
    }
  }
  
}
