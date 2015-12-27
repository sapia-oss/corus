package org.sapia.corus.deployer;

import java.io.File;
import java.util.List;

import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.taskmanager.core.Task;

/**
 * The internal deployer interface, providing methods that are accessible from
 * within the Corus server only.
 * 
 * @author yduchesne
 * 
 */
public interface InternalDeployer extends Deployer {

  /**
   * @param name
   *          a distribution name.
   * @param version
   *          a distribution version.
   * @return the archive {@link File} corresponding to the given distribution
   *         name and version.
   * @throws DistributionNotFoundException
   *           if no such distribution exists.
   */
  public File getDistributionFile(String name, String version) throws DistributionNotFoundException;
  
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
