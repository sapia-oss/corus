package org.sapia.corus.client.facade;

import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.exceptions.deployer.ConcurrentDeploymentException;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.exceptions.deployer.RollbackScriptNotFoundException;
import org.sapia.corus.client.exceptions.deployer.RunningProcessesException;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * This interface specifies a facade to the Corus {@link Deployer}
 * 
 * @author yduchesne
 * 
 */
public interface DeployerFacade {

  /**
   * Proceeds to the deployment of the distribution archive whose file name is
   * given.
   * 
   * @param fileName
   *          the name of the distribution archive.
   * @param prefs
   *          the {@link DeployPreferences} to use.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   * @return a {@link ProgressQueue} instance.
   * @throws java.io.IOException
   *           if a IO error occurs.
   * @throws ConcurrentDeploymentException
   *           if another thread is trying to concurrently deploy the same
   *           archive.
   * @throws DuplicateDistributionException
   *           if a distribution already exists that corresponds to the given
   *           deployment.
   * @throws Exception
   *           if an undefined error occurs.
   */
  public ProgressQueue deployDistribution(String fileName, DeployPreferences prefs, ClusterInfo cluster) throws java.io.IOException, ConcurrentDeploymentException,
      DuplicateDistributionException, Exception;

  /**
   * Proceeds to the deployment of the file whose name is given.
   * 
   * @param fileName
   *          the name of the file to deploy.
   * @param destinationDir
   *          the path to the directory where the file should be uploaded (if
   *          <code>null</code>, the default upload directory on the remote
   *          Corus node will be used).
   * @param cluster
   *          a {@link ClusterInfo} instance.
   * @return a {@link ProgressQueue} instance.
   * @throws java.io.IOException
   *           if a IO error occurs.
   * @throws Exception
   *           if an undefined error occurs.
   */
  public ProgressQueue deployFile(String fileName, String destinationDir, ClusterInfo cluster) throws java.io.IOException, Exception;

  /**
   * Proceeds to the deployment of the script whose name and description are
   * given.
   * 
   * @param fileName
   *          the name of the script to deploy.
   * @param alias
   *          the alias of the script (acts as the script's identifier).
   * @param the
   *          script's description.
   * @param cluster
   *          a {@link ClusterInfo} instance.
   * @return a {@link ProgressQueue} instance.
   * @throws java.io.IOException
   *           if a IO error occurs.
   * @throws Exception
   *           if an undefined error occurs.
   */
  public ProgressQueue deployScript(String fileName, String alias, String description, ClusterInfo cluster) throws java.io.IOException, Exception;

  /**
   * Undeploys the distribution(s) corresponding to the given criteria. It is
   * the client application's responsibility to ensure that all processes
   * corresponding to a given distribution are killed prior to calling this
   * method.
   * <p>
   * Running processes could have locks on files in the distribution's
   * directory, preventing the cleanup procedure from completing successfully. T
   * 
   * @param criteria
   *          a {@link DistributionCriteria}.
   * @param cluster
   *          a {@link ClusterInfo}.
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue undeployDistribution(DistributionCriteria criteria, ClusterInfo cluster) throws RunningProcessesException;

  /**
   * Returns the list of distributions matching the given criteria.
   * 
   * @param cluster
   *          a {@link ClusterInfo} instance.
   * @return a {@link Results} containing {@link Distribution} instance.
   */
  public Results<List<Distribution>> getDistributions(DistributionCriteria criteria, ClusterInfo cluster);
  
  
  /**
   * Triggers execution of the <tt>META-INF/scripts/rollback.corus</tt> script for a given distribution.
   * 
   * @param name the name of the distribution to roll back.
   * @param version the version of the distribution to roll back.
   * @param cluster a {@link ClusterInfo} instance.
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue rollbackDistribution(String name, String version, ClusterInfo cluster) 
      throws RollbackScriptNotFoundException, DistributionNotFoundException;
  

}
