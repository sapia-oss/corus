package org.sapia.corus.repository.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptManager;
import org.sapia.corus.client.services.repository.FileListResponse;
import org.sapia.corus.client.services.repository.ShellScriptDeploymentRequest;
import org.sapia.corus.client.services.repository.ShellScriptListResponse;
import org.sapia.corus.taskmanager.util.RunnableTask;
import org.sapia.ubik.mcast.EventChannel;

/**
 * Task that handles {@link FileListResponse}s.
 * 
 * @author yduchesne
 * 
 */
public class ShellScriptListResponseHandlerTask extends RunnableTask {

  private ShellScriptListResponse scriptsRes;

  /**
   * @param scriptsRes
   *          the {@link FileListResponse} to handle.
   */
  public ShellScriptListResponseHandlerTask(ShellScriptListResponse scriptsRes) {
    this.scriptsRes = scriptsRes;
  }

  @Override
  public void run() {
    ClusterManager cluster = context().getServerContext().getServices().getClusterManager();
    ShellScriptManager manager = context().getServerContext().getServices().getScriptManager();

    Set<ShellScript> toReceive = new HashSet<ShellScript>();

    toReceive.addAll(scriptsRes.getScripts());
    toReceive.removeAll(manager.getScripts());

    context().debug(String.format("Got script list (%s) from %s", scriptsRes.getScripts(), scriptsRes.getEndpoint()));

    if (!toReceive.isEmpty()) {
      context().debug("Sending shell script deployment request");
      EventChannel channel = cluster.getEventChannel();
      ShellScriptDeploymentRequest request = new ShellScriptDeploymentRequest(context().getServerContext().getCorusHost().getEndpoint(),
          new ArrayList<ShellScript>(toReceive));

      try {
        channel.dispatch(scriptsRes.getEndpoint().getChannelAddress(), ShellScriptDeploymentRequest.EVENT_TYPE, request);
      } catch (Exception e) {
        context().error(String.format("Could not send script list to %s", scriptsRes.getEndpoint()), e);
      }
    } else {
      context().debug("No scripts to request from " + scriptsRes.getEndpoint());
    }
  }
}
