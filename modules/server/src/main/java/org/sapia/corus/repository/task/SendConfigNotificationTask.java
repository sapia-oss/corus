package org.sapia.corus.repository.task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.tuple.PairTuple;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.configurator.Tag;
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
  private Set<PairTuple<Boolean, Endpoint>> targets;

  public SendConfigNotificationTask(RepositoryConfiguration config, Set<PairTuple<Boolean, Endpoint>> targets) {
    this.config = config;
    this.targets = targets;
  }

  @Override
  public void run() {
    try {
      List<Property> props = context().getServerContext().getServices().getConfigurator()
          .getAllPropertiesList(PropertyScope.PROCESS, new HashSet<ArgMatcher>());
      Set<Tag>       tags  = context().getServerContext().getServices().getConfigurator().getTags();

      if (props.isEmpty() && tags.isEmpty()) {
        context().debug("No tags or properties to push to: " + targets);
      } else if (!config.isPushTagsEnabled() && !config.isPushPropertiesEnabled()) {
        context().debug("Push of properties and tags is disabled, NOT sending config to: " + targets);
      } else {
        context().debug("Sending configuration notification to: " + targets);
        doSend(true, props, tags, targets.stream().filter(p -> p.get_0()).map(p -> p.get_1()).collect(Collectors.toSet()));
        doSend(false, props, tags, targets.stream().filter(p -> !p.get_0()).map(p -> p.get_1()).collect(Collectors.toSet()));

      }
    } catch (Exception e) {
      context().error("Could not send configuration to targets: " + targets, e);
    }
  }

  private void doSend(boolean force, List<Property> props, Set<Tag> tags, Set<Endpoint> targetEndpoints) throws Exception {
    ConfigNotification notif = new ConfigNotification();
    notif.setForce(force);
    notif.getTargets().addAll(targetEndpoints);

    if (config.isPushPropertiesEnabled()) {
      context().debug("Pushing properties to: " + targetEndpoints);
      notif.addProperties(props);
    } else {
      context().debug("Pushing of properties disabled, NOT pushing to: " + targets);
    }

    if (config.isPushTagsEnabled()) {
      context().debug("Pushing tags to: " + targets);
      notif.addTags(Tag.asStrings(tags));
    } else {
      context().debug("Pushing of tags disabled, NOT pushing to: " + targets);
    }

    context().getServerContext().getServices().getClusterManager().dispatch(notif);
  }
}
