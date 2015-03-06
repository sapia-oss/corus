package org.sapia.corus.repository.task;

import java.util.List;

import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.client.services.processor.ProcessDef;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.taskmanager.util.RunnableTask;

/**
 * Handles a {@link List} of {@link ExecConfig} instance that has been
 * dispatched by a repo server node: this tasks attempts to deploy an exec
 * config if its corresponding distribution has been deployed.
 * <p>
 * The reason for this logic being isolated in a task is that deployments are
 * processed asynchronously: processing of a deployment might not have yet
 * completed when the corresponding execution configurations are received; this
 * task thus ensures that deployment has completed prior to adding the exec
 * configs to the repo client node.
 * 
 * @author yduchesne
 */
public class HandleExecConfigTask extends RunnableTask {

  private enum State {
    PENDING, PROCESSED;
  }

  private RepositoryConfiguration repoConfig;
  private List<ExecConfig> execConfigs;
  private State state = State.PENDING;

  /**
   * @param repoConfig
   *          the {@link RepositoryConfiguration}.
   * @param execConfigs
   *          a {@link List} of {@link ExecConfig}s.
   */
  public HandleExecConfigTask(RepositoryConfiguration repoConfig, List<ExecConfig> execConfigs) {
    this.repoConfig = repoConfig;
    this.execConfigs = execConfigs;
  }

  @Override
  public void run() {
    if (state == State.PROCESSED) {
      context().debug("Execution configurations already processed");
    } else if (isDistributionsAvailable()) {
      context().debug("Distributions available for provided execution configurations");
      for (ExecConfig ec : execConfigs) {
        context().info(String.format("Adding exec config: %s, %s", ec.getName(), ec.getProfile()));
        context().getServerContext().getServices().getProcessor().addExecConfig(ec);
      }
      for (ExecConfig ec : execConfigs) {
        if (ec.isStartOnBoot() && repoConfig.isBootExecEnabled()) {
          context().debug(String.format("Triggering startup for exec config: %s, %s", ec.getName(), ec.getProfile()));
          ExecConfigCriteria crit = ExecConfigCriteria.builder().name(ArgFactory.parse(ec.getName())).build();
          context().getServerContext().getServices().getProcessor().execConfig(crit);
        }
      }
      state = State.PROCESSED;
    }
  }

  boolean isDistributionsAvailable() {
    context().debug("Checking if distributions are available for provided exec configurations");
    for (ExecConfig ec : execConfigs) {
      for (ProcessDef proc : ec.getProcesses()) {
        try {
          context().getServerContext().getServices().getDeployer()
              .getDistribution(DistributionCriteria.builder().name(proc.getDist()).version(proc.getVersion()).build());
          context().debug("Got distribution for config " + ec.getName());
        } catch (DistributionNotFoundException e) {
          context().debug("No distribution yet for config " + ec.getName());
          return false;
        }
      }
    }

    return true;
  }
}
