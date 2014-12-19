package org.sapia.corus.repository.task;

import java.util.List;
import java.util.Set;

import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.client.services.repository.SecurityConfigNotification;
import org.sapia.corus.client.services.security.ApplicationKeyManager.AppKeyConfig;
import org.sapia.corus.client.services.security.SecurityModule.RoleConfig;
import org.sapia.corus.taskmanager.util.RunnableTask;

/**
 * This task sends sends security configuration to repo clients.
 * 
 * @author yduchesne
 *
 */
public class SendSecurityConfigNotificationTask extends RunnableTask {

  private RepositoryConfiguration config;
  private Set<Endpoint> targets;

  public SendSecurityConfigNotificationTask(RepositoryConfiguration config, Set<Endpoint> targets) {
    this.config = config;
    this.targets = targets;
  }
  
  @Override
  public void run() {
    List<RoleConfig> roleConfigs = context()
        .getServerContext().getServices().getSecurityModule().getRoleConfig(ArgFactory.any());
    
    List<AppKeyConfig> appKeyConfigs = context()
        .getServerContext().getServices().getAppKeyManager().getAppKeyConfig(ArgFactory.any());
    
    if (roleConfigs.isEmpty() && appKeyConfigs.isEmpty()) {
      context().debug("No security config to send to: " + targets);
    } else if (config.isPushSecurityConfigEnabled()) {
      context().debug("Sending security configuration notification to: " + targets);
      SecurityConfigNotification notif = new SecurityConfigNotification(roleConfigs, appKeyConfigs);
      notif.getTargets().addAll(targets);
      try {
        context().getServerContext().getServices().getClusterManager().send(notif);
      } catch (Exception e) {
        context().error("Could not send roles to targets: " + targets, e);
      }
    }
    
  }
}
