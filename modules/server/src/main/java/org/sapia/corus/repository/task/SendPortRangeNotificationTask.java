package org.sapia.corus.repository.task;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.sapia.corus.client.common.tuple.PairTuple;
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

  private RepositoryConfiguration           config;
  private Set<PairTuple<Boolean, Endpoint>> targets;

  public SendPortRangeNotificationTask(RepositoryConfiguration config, Set<PairTuple<Boolean, Endpoint>> targets) {
    this.config = config;
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
        doSendNotif(true, ranges, targets.stream().filter(p -> p.get_0()).map(p -> p.get_1()).collect(Collectors.toSet()));
        doSendNotif(false, ranges, targets.stream().filter(p -> !p.get_0()).map(p -> p.get_1()).collect(Collectors.toSet()));
      }
    } catch (Exception e) {
      context().error("Could not send port ranges", e);
    }

  }
  
  private void doSendNotif(boolean force, List<PortRange> ranges, Set<Endpoint> targetEndpoints) throws Exception {
    PortRangeNotification notif = new PortRangeNotification(ranges);
    notif.setForce(force);
    notif.getTargets().addAll(targetEndpoints);
    context().getServerContext().getServices().getClusterManager().dispatch(notif);    
  }
}
