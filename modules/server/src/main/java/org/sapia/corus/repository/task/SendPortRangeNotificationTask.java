package org.sapia.corus.repository.task;

import java.util.List;
import java.util.Set;

import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.services.repository.PortRangeNotification;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.taskmanager.util.RunnableTask;

/**
 * Sends port ranges to requesting repo clients.
 * 
 * @author yduchesne
 *
 */
public class SendPortRangeNotificationTask extends RunnableTask {

  private RepositoryConfiguration config;
  private Set<Endpoint> targets;
  
  public SendPortRangeNotificationTask(RepositoryConfiguration config, Set<Endpoint> targets) {
    this.config  = config;
    this.targets = targets;
  }
  
  @Override
  public void run() {
    PortManager portManager = super.context().getServerContext().getServices().getPortManager();
    
    try {
      if (!config.isPushPortRangesEnabled()) {
        context().debug("Not sending port ranges (push disabled)");
        return;
      }
      List<PortRange> ranges = portManager.getPortRanges();
      if (ranges.isEmpty()) {
        context().debug("No port ranges to send to: " + targets);
      } else {
        context().debug("Sending port range notification to: " + targets);
        PortRangeNotification notif   = new PortRangeNotification(ranges);
        notif.getTargets().addAll(targets);
        context()
          .getServerContext()
          .getServices()
          .getClusterManager()
          .send(notif);
      }
    } catch (Exception e) {
      context().error("Could not send port ranges", e);
    }
    
  }
}
