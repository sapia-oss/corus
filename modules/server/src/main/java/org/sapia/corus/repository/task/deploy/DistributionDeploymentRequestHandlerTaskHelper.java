package org.sapia.corus.repository.task.deploy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ExecConfigCriteria;
import org.sapia.corus.client.services.processor.ProcessDef;
import org.sapia.corus.client.services.repository.DistributionDeploymentRequest;
import org.sapia.corus.client.services.repository.RepoDistribution;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.deployer.InternalDeployer;
import org.sapia.corus.repository.task.DistributionRequestHandlerTask;
import org.sapia.corus.repository.task.SendExecConfigNotificationTask;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.util.CompositeTask;
import org.sapia.corus.taskmanager.util.RunnableTask;
import org.sapia.corus.util.CollectionUtil;
import org.sapia.ubik.util.Condition;

/**
 * Creates the subtasks pertaining to distribution deployment.
 * 
 * @author yduchesne
 * 
 */
public class DistributionDeploymentRequestHandlerTaskHelper extends ArtifactDeploymentHandlerTaskHelper {

  private List<DistributionDeploymentRequest> requests;

  public DistributionDeploymentRequestHandlerTaskHelper(RepositoryConfiguration config, TaskExecutionContext context,
      List<DistributionDeploymentRequest> requests) {
    super(config, context);
    this.requests = requests;
  }

  @Override
  public void addTo(CompositeTask toAddTo) {
    InternalDeployer deployer = context().getServerContext().lookup(InternalDeployer.class);

    List<ExecConfig> execConfigs = context().getServerContext().getServices().getProcessor().getExecConfigs(ExecConfigCriteria.builder().all().build());

    Map<RepoDistribution, Set<Endpoint>> distributionTargets = getDistributionTargets(requests);

    context().info(String.format("Got %s targets to deploy to", distributionTargets));

    for (final Map.Entry<RepoDistribution, Set<Endpoint>> entry : distributionTargets.entrySet()) {
      context().info(String.format("Triggering deployment of %s to %s", entry.getKey(), entry.getValue()));
      try {
        RunnableTask task = new DistributionRequestHandlerTask(deployer.getDistributionFile(entry.getKey().getName(), entry.getKey().getVersion()),
            new ArrayList<Endpoint>(entry.getValue()));
        toAddTo.add(task);
        List<ExecConfig> execConfigsForDistribution = CollectionUtil.filterToArrayList(execConfigs, new Condition<ExecConfig>() {
          @Override
          public boolean apply(ExecConfig item) {
            for (ProcessDef p : item.getProcesses()) {
              if (p.getDist().equals(entry.getKey().getName()) && p.getVersion().equals(entry.getKey().getVersion())) {
                return true;
              }
            }
            return false;
          }
        });
        if (!execConfigsForDistribution.isEmpty()) {
          toAddTo.add(new SendExecConfigNotificationTask(execConfigsForDistribution, entry.getValue()));
        }
      } catch (DistributionNotFoundException e) {
        context().error("Caught error attempting to initiate distribution deployment", e);
      }
    }
  }

  // --------------------------------------------------------------------------
  // Package visibility for unit testing

  Map<RepoDistribution, Set<Endpoint>> getDistributionTargets(List<DistributionDeploymentRequest> requests) {
    Map<RepoDistribution, Set<Endpoint>> distributionTargets = new HashMap<RepoDistribution, Set<Endpoint>>();
    for (DistributionDeploymentRequest req : requests) {
      context().debug("Processing distribution deployment request: " + req);
      for (RepoDistribution dist : req.getDistributions()) {
        context().debug(String.format("Adding target %s for deployment of distribution %s", req.getEndpoint(), dist));
        Set<Endpoint> targets = distributionTargets.get(dist);
        if (targets == null) {
          targets = new HashSet<Endpoint>();
          distributionTargets.put(dist, targets);
        }
        targets.add(req.getEndpoint());
      }

    }
    return distributionTargets;
  }

}
