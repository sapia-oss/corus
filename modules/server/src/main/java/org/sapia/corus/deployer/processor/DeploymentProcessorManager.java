package org.sapia.corus.deployer.processor;

import org.sapia.corus.client.common.LogCallback;

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

}
