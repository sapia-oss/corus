package org.sapia.corus.deployer.processor;

import java.util.List;

import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.taskmanager.core.Task;

/**
 * An instance of this class is internally called when a Corus repo server node wishes to replicate container images
 * to Corus repo client nodes.
 * 
 * @author yduchesne
 *
 */
public interface ImageDeploymentReplicationProcessor {

  /**
   * @param context the current {@link DeploymentContext}. 
   * 
   * @return <code>true</code> if this instance accepts the given context.
   */
  public boolean accepts(DeploymentContext context);
  
  /**
   * This method will return a task in charge of replicating the deployment of container images to the provided
   * Corus repo clients.
   * 
   * @param context the current {@link DeploymentContext}.
   * @param endpoints the {@link List} of Corus repo client endpoints to which the images are targeted.
   * @return a Task of container image deployment {@link Task}s, if any apply.
   */
  public Task<Void, Void> getImageDeploymentTaskFor(DeploymentContext context, List<Endpoint> endpoints);
}
