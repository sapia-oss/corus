package org.sapia.corus.deployer.processor;

import java.util.List;

import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.taskmanager.core.Task;

/**
 * An implementation of this interface is expected to use {@link DeploymentPostProcessor}s 
 * and {@link UndeploymentPostProcessor}s in order to fulfill its contract.
 * 
 * @author yduchesne
 *
 */
public interface DeploymentProcessorManager {
  
  public static final String ROLE = DeploymentProcessorManager.class.getName();
  
  /**
   * @param context a {@link DeploymentContext}.
   * @param callback a {@link LogCallback} to log to for informing about progress.
   * @throws Exception if an error occurs while performing this operation.
   */
  public void onPostDeploy(DeploymentContext context, LogCallback callback) throws Exception;
  
  /**
   * @param context a {@link DeploymentContext}.
   * @param callback a {@link LogCallback} to log to for informing about progress.
   * @throws Exception if an error occurs while performing this operation.
   */
  public void onPostUndeploy(DeploymentContext context, LogCallback callback) throws Exception;

  /**
   * This method will return a list of tasks in charge of replicating the deployment of container images to the provided
   * Corus repo clients.
   * 
   * @param dist a {@link Distribution}.
   * @param endpoints the {@link List} of Corus repo client endpoints to which the images are targeted.
   * @return a {@link List} of container image deployment {@link Task}s, if any apply.
   */
  public List<Task<Void, Void>> getImageDeploymentTasksFor(Distribution dist, List<Endpoint> endpoints);
}
