package org.sapia.corus.deployer;

import org.sapia.corus.LogicException;
import org.sapia.corus.Module;
import org.sapia.corus.admin.CommandArg;
import org.sapia.corus.deployer.config.Distribution;
import org.sapia.corus.util.ProgressQueue;

import java.util.List;

/**
 * The deployer is in charge of processing incoming deployment .jars; it stores
 * the jars under a <code>tmp</code> directory, and therefafter proceeds to
 * deployment per say (extracting the .jar under the deployment directory).
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface Deployer extends java.rmi.Remote, Module {
  String ROLE = Deployer.class.getName();

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
  public ProgressQueue undeploy(CommandArg distName, CommandArg version);

  /**
   * Returns the list of distributions that this instance contains.
   *
   * @return a <code>List</code> of <code>Distribution</code> instances.
   */
  public List getDistributions();

  /**
   * Returns the list of distributions with the given name.
   *
   * @param name a distribution name.
   * @return a <code>List</code> of <code>Distribution</code> instances.
   */
  public List getDistributions(CommandArg name);
  
  /**
   * Returns the list of distributions with the given name.
   *
   * @param name a distribution name.
   * @param version a distribution version.
   * @return a <code>List</code> of <code>Distribution</code> instances.
   */
  public List getDistributions(CommandArg name, CommandArg version);  
  

  /**
   * Returns the distribution with the given name and version.
   *
   * @param name a distribution name.
   * @param name a distribution version.
   * @return a <code>Distribution</code> instance.
   * @throws LogicException if no corresponding distribution could be found.
   */
  public Distribution getDistribution(CommandArg name, CommandArg version)
                               throws LogicException;
}
