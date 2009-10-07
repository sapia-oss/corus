package org.sapia.corus.admin.services.deployer;

import java.util.List;

import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.Module;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.exceptions.LogicException;
import org.sapia.corus.util.ProgressQueue;

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
   * Undeploys the distribution corresponding to the given name and version.
   * It is the client application's responsibility to ensure that all VMs
   * corresponding to a given distribution are killed prior to calling
   * this method.
   * <p>
   * Running VMs could have locks on files in the distribution's directory,
   * preventing the cleanup procedure from completing successfully.
   *
   * @param distName the name of the distribution to undeploy.
   * @param version  the name of the version to undeploy.
   * @return a <code>ProgressQueue</code>.
   */
  public ProgressQueue undeploy(Arg distName, Arg version);

  /**
   * Returns the list of distributions that this instance contains.
   *
   * @return a <code>List</code> of <code>Distribution</code> instances.
   */
  public List<Distribution> getDistributions();

  /**
   * Returns the list of distributions with the given name.
   *
   * @param name a distribution name.
   * @return a <code>List</code> of <code>Distribution</code> instances.
   */
  public List<Distribution> getDistributions(Arg name);
  
  /**
   * Returns the list of distributions with the given name.
   *
   * @param name a distribution name.
   * @param version a distribution version.
   * @return a <code>List</code> of <code>Distribution</code> instances.
   */
  public List<Distribution> getDistributions(Arg name, Arg version);  
  

  /**
   * Returns the distribution with the given name and version.
   *
   * @param name a distribution name.
   * @param name a distribution version.
   * @return a <code>Distribution</code> instance.
   * @throws LogicException if no corresponding distribution could be found.
   */
  public Distribution getDistribution(Arg name, Arg version)
                               throws LogicException;
}
