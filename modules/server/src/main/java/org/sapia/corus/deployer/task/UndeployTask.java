package org.sapia.corus.deployer.task;

import java.io.File;
import java.util.List;

import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.event.UndeploymentCompletedEvent;
import org.sapia.corus.client.services.deployer.event.UndeploymentFailedEvent;
import org.sapia.corus.client.services.deployer.event.UndeploymentStartingEvent;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.deployer.DeployerThrottleKeys;
import org.sapia.corus.deployer.DistributionDatabase;
import org.sapia.corus.deployer.processor.DeploymentContext;
import org.sapia.corus.deployer.processor.DeploymentProcessorManager;
import org.sapia.corus.taskmanager.TaskLogCallback;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;
import org.sapia.corus.taskmanager.core.ThrottleKey;
import org.sapia.corus.taskmanager.core.Throttleable;

/**
 * This tasks remove a distribution from the Corus server.
 * 
 * @author Yanick Duchesne
 */
public class UndeployTask extends Task<Void, TaskParams<DistributionCriteria, Void, Void, Void>> implements Throttleable {

  @Override
  public ThrottleKey getThrottleKey() {
    return DeployerThrottleKeys.UNDEPLOY_DISTRIBUTION;
  }

  @Override
  public Void execute(TaskExecutionContext ctx, TaskParams<DistributionCriteria, Void, Void, Void> params) throws Throwable {
    
    DistributionCriteria       criteria  = params.getParam1();
    FileSystemModule           fs        = ctx.getServerContext().getServices().getFileSystem();
    DistributionDatabase       db        = ctx.getServerContext().getServices().getDistributions();
    DeployerConfiguration      config    = ctx.getServerContext().getServices().getDeployer().getConfiguration();
    DeploymentProcessorManager processor = ctx.getServerContext().getServices().getDeploymentProcessorManager();
    
    List<Distribution> dists = db.getDistributions(criteria);
    for (Distribution dist : dists) {
      try {
        DeploymentContext deployContext = new DeploymentContext(dist);
        try {
          processor.onPostUndeploy(deployContext, new TaskLogCallback(ctx).failsafe());
        } catch (Exception e) {
          ctx.warn("Got non-critical error while undeploying (moving ahead with remaining undeployment steps)", e);
        }
        ctx.getServerContext().getServices().getEventDispatcher().dispatch(new UndeploymentStartingEvent(dist));
        File distDir = new File(dist.getBaseDir());
        ctx.info(String.format("Undeploying distribution %s", dist.getDislayInfo()));
        fs.deleteDirectory(distDir);
        fs.deleteFile(FilePath.newInstance().addDir(config.getRepoDir()).setRelativeFile(dist.getDistributionFileName()).createFile());
        ctx.info("Undeployment successful");
        ctx.getServerContext().getServices().getEventDispatcher().dispatch(new UndeploymentCompletedEvent(dist));
      } catch (Throwable err) {
        ctx.getServerContext().getServices().getEventDispatcher().dispatch(new UndeploymentFailedEvent(dist));
        throw err;
      } 
    }
    db.removeDistribution(criteria);
    
    return null;
  }
}
