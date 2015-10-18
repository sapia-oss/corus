package org.sapia.corus.deployer.processor;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.sapia.corus.client.common.LogCallback;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Implementation of the {@link DeploymentProcessorManager} interface.
 * 
 * @author yduchesne
 *
 */
public class DeploymentProcessorManagerImpl implements DeploymentProcessorManager, ApplicationContextAware {
  
  private ApplicationContext appContext;
  
  private List<DeploymentPostProcessor>   deploymentPostProcessors   = new ArrayList<DeploymentPostProcessor>();
  private List<UndeploymentPostProcessor> undeploymentPostProcessors = new ArrayList<UndeploymentPostProcessor>();
  
  @PostConstruct
  public void init() {
    deploymentPostProcessors.addAll(appContext.getBeansOfType(DeploymentPostProcessor.class).values());
    undeploymentPostProcessors.addAll(appContext.getBeansOfType(UndeploymentPostProcessor.class).values());
  }
  
  // --------------------------------------------------------------------------
  // ApplicationContextAware interface
  
  @Override
  public void setApplicationContext(ApplicationContext appContext) throws BeansException {
    this.appContext = appContext;
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

}
