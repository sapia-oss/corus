package org.sapia.corus.deployer.processor;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.taskmanager.core.Task;

/**
 * Implementation of the {@link DeploymentProcessorManager} interface.
 * 
 * @author yduchesne
 *
 */
@Bind(moduleInterface = { DeploymentProcessorManager.class })
public class DeploymentProcessorManagerImpl extends ModuleHelper implements DeploymentProcessorManager {
  
  private List<DeploymentPostProcessor>   deploymentPostProcessors   = new ArrayList<DeploymentPostProcessor>();
  private List<UndeploymentPostProcessor> undeploymentPostProcessors = new ArrayList<UndeploymentPostProcessor>();
  private List<ImageDeploymentReplicationProcessor> imageDeploymentReplicationProcessors = new ArrayList<ImageDeploymentReplicationProcessor>();

  // --------------------------------------------------------------------------
  // Module methods

  @Override
  public String getRoleName() {
    return ROLE;
  }

  @Override
  public void init() {
    deploymentPostProcessors.addAll(appContext.getBeansOfType(DeploymentPostProcessor.class).values());
    undeploymentPostProcessors.addAll(appContext.getBeansOfType(UndeploymentPostProcessor.class).values());
    imageDeploymentReplicationProcessors.addAll(appContext.getBeansOfType(ImageDeploymentReplicationProcessor.class).values());
    for (DeploymentPostProcessor p : deploymentPostProcessors) {
      logger().info("Got DeploymentPostProcessor: " + p);
    }
    
    for (UndeploymentPostProcessor p : undeploymentPostProcessors) {
      logger().info("Got UndeploymentPostProcessor: " + p);
    }
  }
  
  @Override
  public void dispose() throws Exception {
  }
  
  // --------------------------------------------------------------------------
  // DeploymentProcessorManager interface
  
  @Override
  public void onPostDeploy(DeploymentContext context, LogCallback callback) throws Exception {
    for (DeploymentPostProcessor d : deploymentPostProcessors) {
      d.onPostDeploy(context, callback);
    }
  }
  
  @Override
  public void onPostUndeploy(DeploymentContext context, LogCallback callback) throws Exception {
    for (UndeploymentPostProcessor u : undeploymentPostProcessors) {
      u.onPostUndeploy(context, callback);
    }
  }
  
  @Override
  public List<Task<Void, Void>> getImageDeploymentTasksFor(Distribution dist, List<Endpoint> endpoints) {
    List<Task<Void, Void>> toReturn = new ArrayList<>();
    DeploymentContext ctx = new DeploymentContext(dist);
    for (ImageDeploymentReplicationProcessor idp : imageDeploymentReplicationProcessors) {
      if (idp.accepts(ctx)) {
        toReturn.add(idp.getImageDeploymentTaskFor(ctx, endpoints));
      }
    }
    return toReturn;
  }

}
