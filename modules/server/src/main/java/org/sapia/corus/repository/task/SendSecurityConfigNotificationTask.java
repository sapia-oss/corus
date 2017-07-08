package org.sapia.corus.repository.task;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.tuple.PairTuple;
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
  private Set<PairTuple<Boolean, Endpoint>> targets;

  public SendSecurityConfigNotificationTask(RepositoryConfiguration config, Set<PairTuple<Boolean, Endpoint>> targets) {
    this.config  = config;
    this.targets = targets;
  }
  
  @Override
  public void run() {
    List<RoleConfig> roleConfigs = context()
        .getServerContext().getServices().getSecurityModule().getRoleConfig(ArgMatchers.any());
    
    List<AppKeyConfig> appKeyConfigs = context()
        .getServerContext().getServices().getAppKeyManager().getAppKeyConfig(ArgMatchers.any());
    
    if (roleConfigs.isEmpty() && appKeyConfigs.isEmpty()) {
      context().debug("No security config to send to: " + targets);
    } else if (config.isPushSecurityConfigEnabled()) {
      context().debug("Sending security configuration notification to: " + targets);
      try {
        doSend(true, roleConfigs, appKeyConfigs, targets.stream().filter(p -> p.get_0()).map(p -> p.get_1()).collect(Collectors.toSet()));
        doSend(false, roleConfigs, appKeyConfigs, targets.stream().filter(p -> !p.get_0()).map(p -> p.get_1()).collect(Collectors.toSet()));        
      } catch (Exception e) {
        context().error("Could not send roles to targets: " + targets, e);
      }
    }
    
  }
  
  private void doSend(boolean force, List<RoleConfig> roleConfigs, List<AppKeyConfig> appKeyConfigs, Set<Endpoint> targets) throws Exception {
    SecurityConfigNotification notif = new SecurityConfigNotification(roleConfigs, appKeyConfigs);
    notif.getTargets().addAll(targets);
    notif.setForce(force);
    context().getServerContext().getServices().getClusterManager().send(notif);    
  }
}
