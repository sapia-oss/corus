package org.sapia.corus.repository.task;

import java.util.Properties;
import java.util.Set;

import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.repository.ConfigNotification;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.taskmanager.util.RunnableTask;

/**
 * Sends a {@link ConfigNotification} to targeted nodes.
 * 
 * @author yduchesne
 * 
 */
public class SendConfigNotificationTask extends RunnableTask {

  private RepositoryConfiguration config;
  private Set<Endpoint> targets;

  public SendConfigNotificationTask(RepositoryConfiguration config, Set<Endpoint> targets) {
    this.config = config;
    this.targets = targets;
  }

  @Override
  public void run() {
    try {
      Properties props = context().getServerContext().getProcessProperties();
      Set<String> tags = context().getServerContext().getServices().getConfigurator().getTags();

      if (props.isEmpty() && tags.isEmpty()) {
        context().debug("No tags or properties to push to: " + targets);
      } else if (!config.isPushTagsEnabled() && !config.isPushPropertiesEnabled()) {
        context().debug("Push of properties and tags is disabled, NOT sending config to: " + targets);
      } else {
        context().debug("Sending configuration notification to: " + targets);
        ConfigNotification notif = new ConfigNotification();
        notif.getTargets().addAll(targets);

        if (config.isPushPropertiesEnabled()) {
          context().debug("Pushing properties to: " + targets);
          notif.addProperties(props);
        } else {
          context().debug("Pushing of properties disabled, NOT pushing to: " + targets);
        }

        if (config.isPushTagsEnabled()) {
          context().debug("Pushing tags to: " + targets);
          notif.addTags(tags);
        } else {
          context().debug("Pushing of tags disabled, NOT pushing to: " + targets);
        }

        context().getServerContext().getServices().getClusterManager().send(notif);
      }
    } catch (Exception e) {
      context().error("Could not send configuration to targets: " + targets, e);
    }
  }

}
