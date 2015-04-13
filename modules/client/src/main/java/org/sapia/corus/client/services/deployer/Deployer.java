package org.sapia.corus.client.services.deployer;

import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Module;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.exceptions.deployer.RollbackScriptNotFoundException;
import org.sapia.corus.client.exceptions.deployer.RunningProcessesException;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * The deployer is in charge of processing incoming deployment .jars; it stores
 * the jars under a <code>tmp</code> directory, and therefafter proceeds to
 * deployment per say (extracting the .jar under the deployment directory).
 * 
 * @author Yanick Duchesne
 */
public interface Deployer extends java.rmi.Remote, Module {

  String ROLE = Deployer.class.getName();

  /**
   * @return this instance's configuration.
   */
  public DeployerConfiguration getConfiguration();

  /**
   * Undeploys the distribution corresponding to the given name and version. It
   * is the client application's responsibility to ensure that all VMs
   * corresponding to a given distribution are killed prior to calling this
   * method.
   * <p>
   * Running VMs could have locks on files in the distribution's directory,
   * preventing the cleanup procedure from completing successfully.
   * 
   * @param criteria
   *          a {@link DistributionCriteria}
   * @param prefs
   *          the {@link UndeployPreferences} to use.
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue undeploy(DistributionCriteria criteria, UndeployPreferences prefs) throws RunningProcessesException;

  /**
   * Returns the list of distributions with the given name.
   * 
   * @param criteria
   *          a {@link DistributionCriteria}.
   * @return a {@link List} of {@link Distribution} instances.
   */
  public List<Distribution> getDistributions(DistributionCriteria criteria);

  /**
   * Returns the distribution with the given name and version.
   * 
   * @param criteria
   *          a {@link DistributionCriteria}.
   * @return a {@link Distribution} instance.
   * @throws LogicException
   *           if no corresponding distribution could be found.
   */
  public Distribution getDistribution(DistributionCriteria criteria) throws DistributionNotFoundException;
  
  /**
   * Triggers execution of the <tt>META-INF/scripts/rollback.corus</tt> script for a given distribution.
   * 
   * @param name the name of the distribution to roll back.
   * @param version the version of the distribution to roll back.
   * @param cluster a {@link ClusterInfo} instance.
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue rollbackDistribution(String name, String version) throws RollbackScriptNotFoundException, DistributionNotFoundException;
  
  /**
   * @param revId the {@link RevId} corresponding to the revision to unarchive.
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue unarchiveDistributions(RevId revId);
}
