package org.sapia.corus.client.facade;

import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.exceptions.deployer.ConcurrentDeploymentException;
import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.exceptions.deployer.RunningProcessesException;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * This interface specifies a facade to the Corus {@link Deployer}
 * 
 * @author yduchesne
 *
 */
public interface DeployerFacade {
  
  /**
   * Proceeds to the deployment of the distribution archive whose file name
   * is given.
   *
   * @param fileName the name of the distribution archive.
   * @param cluster a <code>ClusterInfo</code> instance.
   * @return a <code>ProgressQueue</code> instance.
   * @throws java.io.IOException if a IO error occurs.
   * @throws ConcurrentDeploymentException if another thread is trying to concurrently
   * deploy the same archive.
   */
  public ProgressQueue deploy(String fileName, ClusterInfo cluster)
                       throws java.io.IOException, 
                              ConcurrentDeploymentException, 
                              DuplicateDistributionException, 
                              Exception;

  /**
   * Undeploys the distribution corresponding to the given name and version.
   * It is the client application's responsibility to ensure that all processes
   * corresponding to a given distribution are killed prior to calling
   * this method.
   * <p>
   * Running processes could have locks on files in the distribution's directory,
   * preventing the cleanup procedure from completing successfully.
   *
   * @param distName the name of the distribution to undeploy.
   * @param version  the name of the version to undeploy.
   * @param cluster a <code>ClusterInfo</code> instance.
   * @return a <code>ProgressQueue</code>.
   */
  public ProgressQueue undeploy(String distName, String version, ClusterInfo cluster) throws RunningProcessesException;
  
  /**
   * Returns the list of distributions.
   * 
   * @param cluster a {@link ClusterInfo} instance.
   * @return a {@link Results} containing {@link Distribution} instance.
   */
  public Results<List<Distribution>> getDistributions(ClusterInfo cluster);

  /**
   * Returns the list of distributions with the given name.
   *
   * @param name a distribution name.
   * @param cluster a {@link ClusterInfo} instance.
   * @return a {@link Results} containing {@link Distribution} instance.
   */
  public Results<List<Distribution>> getDistributions(String name, ClusterInfo cluster);

  /**
   * Returns the distribution(s) with the given name and version.
   *
   * @param name a distribution name.
   * @param name a distribution version.
   * @param cluster a {@link ClusterInfo} instance.
   * @return a {@link Results} containing {@link Distribution} instance.
   */
  public Results<List<Distribution>> getDistributions(String name, String version, ClusterInfo cluster);


}
