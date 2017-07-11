package org.sapia.corus.repository.task;

import java.util.List;
import java.util.Set;

import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.repository.ExecConfigNotification;
import org.sapia.corus.taskmanager.util.RunnableTask;

/**
 * Sends execution configurations to requesting repo clients.
 * 
 * @author yduchesne
 * 
 */
public class SendExecConfigNotificationTask extends RunnableTask {

  private List<ExecConfig> execConfigs;
  private Set<Endpoint>    targets;

  public SendExecConfigNotificationTask(List<ExecConfig> configs, Set<Endpoint> targets) {
    this.execConfigs = configs;
    this.targets     = targets;
  }

  @Override
  public void run() {
    try {
      if (execConfigs.isEmpty()) {
        context().debug("No execution configurations to send to: " + targets);
      } else {
        context().debug("Sending execution configuration notification to: " + targets);
        ExecConfigNotification notif = new ExecConfigNotification(execConfigs);
        notif.getTargets().addAll(targets);
        context().getServerContext().getServices().getClusterManager().dispatch(notif);
      }
    } catch (Exception e) {
      context().error("Could not send execution configurations", e);
    }

  }
}
