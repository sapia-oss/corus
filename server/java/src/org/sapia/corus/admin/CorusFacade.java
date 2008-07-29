package org.sapia.corus.admin;

import java.util.Collection;

import org.sapia.corus.ClusterInfo;
import org.sapia.corus.CorusException;
import org.sapia.corus.LogicException;
import org.sapia.corus.cron.CronJobInfo;
import org.sapia.corus.cron.InvalidTimeException;
import org.sapia.corus.deployer.ConcurrentDeploymentException;
import org.sapia.corus.port.PortActiveException;
import org.sapia.corus.port.PortRangeConflictException;
import org.sapia.corus.port.PortRangeInvalidException;
import org.sapia.corus.processor.ProcStatus;
import org.sapia.corus.processor.Process;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.ubik.net.ServerAddress;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface CorusFacade {
  /**
   * Returns the address of this instance's Corus server.
   *
   * @return this instance's Corus server's <code>ServerAddress</code>.
   */
  public ServerAddress getServerAddress();

  /**
   * Returns the addresses of the Corus servers in the cluster - except
   * the one to which this instance is connected.
   *
   * @return a <code>Collection</code> of<code>ServerAddress</code>es.
   */
  public Collection getServerAddresses();

  /**
   * Returns the domain name of this instance's Corus server.
   *
   * @return this instance's Corus server's domain name.
   */
  public String getDomain();

  /*////////////////////////////////////////////////////////////////////
                           DISTRIBUTION MANAGEMENT
  ////////////////////////////////////////////////////////////////////*/

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
                              ConcurrentDeploymentException, CorusException;

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
  public ProgressQueue undeploy(String distName, String version, ClusterInfo cluster);

  /**
   * Returns the list of distributions.
   * 
   * @param cluster a <code>ClusterInfo</code> instance.
   * @return a <code>Results</code> instance containing <code>ServerAddress</code>es of <code>Distribution</code> instances.
   */
  public Results getDistributions(ClusterInfo cluster);

  /**
   * Returns the list of distributions with the given name.
   *
   * @param name a distribution name.
   * @param cluster a <code>ClusterInfo</code> instance.
   * @return a <code>Results</code> instance containing <code>ServerAddress</code>es of <code>Distribution</code> instances.
   */
  public Results getDistributions(String name, ClusterInfo cluster);

  /**
   * Returns the distribution(s) with the given name and version.
   *
   * @param name a distribution name.
   * @param name a distribution version.
   * @param cluster a <code>ClusterInfo</code> instance.
   *
   * @return a <code>Results</code> of <code>HostItem</code> instances, each encapsulating a <code>Distribution</code> instance.
   * @throws LogicException if no corresponding distribution could be found.
   */
  public Results getDistributions(String name, String version, ClusterInfo cluster);

  /*////////////////////////////////////////////////////////////////////
                            PROCESS MANAGEMENT
  ////////////////////////////////////////////////////////////////////*/

  /**
   * Return the process whose identifier is given..
   *
   * @param vmId a process identifier.
   * @return a <code>Process</code> instance.
   */
  public Process getProcess(String vmId) throws LogicException;

  /**
   * Returns all process objects, per corus server.
   *
   * @param cluster a <code>ClusterInfo</code> instance.
   * @return a <code>Results</code> instance containing <code>ServerAddress</code>es of <code>Process</code> instances.
   * @see org.sapia.corus.processor.Process
   */
  public Results getProcesses(ClusterInfo cluster);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return processes.
   * @param cluster a <code>ClusterInfo</code> instance.
   * 
   * @return a <code>Results</code> instance containing <code>ServerAddress</code>es of <code>Process</code> instances.
   * @see org.sapia.corus.processor.Process
   */
  public Results getProcesses(String distName, ClusterInfo cluster);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return processes.
   * @param version the version of the distribution for which to return processes.
   * @param cluster a <code>ClusterInfo</code> instance.
   * 
   * @return a <code>Results</code> instance containing <code>ServerAddress</code>es of <code>Process</code> instances.
   * @see org.sapia.corus.processor.Process
   */
  public Results getProcesses(String distName, String version, ClusterInfo cluster);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return processes.
   * @param version the version of the distribution for which to return processes.
   * @param profile the profile for which to return VM processes.
   * @param cluster a <code>ClusterInfo</code> instance.
   * @return a <code>Results</code> instance containing <code>ServerAddress</code>es of <code>Process</code> instances.
   * @see org.sapia.corus.processor.Process
   */
  public Results getProcesses(String distName, String version, String profile,
                              ClusterInfo cluster);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return processes.
   * @param version the version of the distribution for which to return processes.
   * @param profile the profile for which to return VM processes.
   * @param processName the name of the VM for which to return process instances.
   * @param cluster a <code>ClusterInfo</code> instance.
   * @return a <code>Results</code> instance containing <code>ServerAddress</code>es of <code>Process</code> instances.
   * @see org.sapia.corus.processor.Process
   */
  public Results getProcesses(String distName, String version, String profile,
                              String processName, ClusterInfo cluster);

  /**
   * Starts process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to start
   * new process(es).
   * @param version the version of the distribution for which to start
   * new process(es).
   * @param profile the name of the profile under which the new process(es)
   * should be started.
   * @param the number of process(es) to start.
   * @param cluster a <code>ClusterInfo</code> instance.
   *
   * @return a <code>ProgressQueue</code>.
   */
  public ProgressQueue exec(String distName, String version, String profile,
                            int instances, ClusterInfo cluster);

  /**
   * Starts process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to start
   * new process(es).
   * @param version the version of the distribution for which to start
   * new process(es).
   * @param profile the name of the profile under which the new process(es)
   * should be started.
   * @param processName the name of the VM configuration for which new process(es)
   * should be started.
   * @param the number of process(es) to start.
   * @param cluster a <code>ClusterInfo</code> instance.
   *
   * @return a <code>ProgressQueue</code>.
   */
  public ProgressQueue exec(String distName, String version, String profile,
                            String processName, int instances, ClusterInfo cluster);

  /**
   * Restarts all suspended processes.
   * 
   * @param cluster a <code>ClusterInfo</code> instance.
   * @return a <code>ProgressQueue</code>.
   */
  public ProgressQueue restart(ClusterInfo cluster);

  /**
   * Kills the process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to kill
   * running processes.
   * @param version the version of the distribution for which to kill
   * running processes.
   * @param profile the name of the profile for which to kill the running process(es).
   * @param cluster a <code>ClusterInfo</code> instance.
   *
   * @return a <code>ProgressQueue</code>.
   */
  public void kill(String distName, String version, String profile,
                   ClusterInfo cluster);

  /**
   * Kills the process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to kill
   * running processes.
   * @param version the version of the distribution for which to kill
   * running processes.
   * @param profile the name of the profile for which to kill the running process(es).
   * @param processName the name of the VM configuration for which to kill the running process(es).
   * @param cluster a <code>ClusterInfo</code> instance.
   *
   * @return a <code>ProgressQueue</code>.
   */
  public void kill(String distName, String version, String profile,
                   String processName, ClusterInfo cluster);

  /**
   * Kills the process with the given identifier.
   *
   * @param a process identifier.
   */
  public void kill(String processId) throws LogicException;

  /**
   * Suspends the process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to kill
   * running processes.
   * @param version the version of the distribution for which to kill
   * running processes.
   * @param profile the name of the profile for which to kill the running process(es).
   * @param cluster a <code>ClusterInfo</code> instance.
   *
   * @return a <code>ProgressQueue</code>.
   */
  public void suspend(String distName, String version, String profile,
                      ClusterInfo cluster);

  /**
   * Suspends the process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to kill
   * running processes.
   * @param version the version of the distribution for which to kill
   * running processes.
   * @param profile the name of the profile for which to kill the running process(es).
   * @param processName the name of the process configuration for which to kill the running process(es).
   * @param cluster a <code>ClusterInfo</code> instance.
   *
   * @return a <code>ProgressQueue</code>.
   */
  public void suspend(String distName, String version, String profile,
                      String processName, ClusterInfo cluster);

  /**
   * Suspends the process with the given identifier.
   *
   * @param dynId a process identifier.
   */
  public void suspend(String dynId);
  
  /*////////////////////////////////////////////////////////////////////
                            CRON MANAGEMENT
  ////////////////////////////////////////////////////////////////////*/

  /**
   * Adds a "cron job" (scheduled process) to the corus.
   *
   * @param <code>CronJobInfo</code> instance.
   */
  public void addCronJon(CronJobInfo info) throws InvalidTimeException;

  /**
   * Removes the cron job corresponding to the given identifier.
   *
   * @param dynId the identifier of the cron job to remove.
   */
  public void removeCronJob(String dynId);

  /**
   * List the currently configured cron jobs.
   *
   * @param cluster a <code>ClusterInfo</code> instance.
   * all the coruss in the cluster.
   *
   * @return a <code>Results</code> instance containing <code>HostList</code>s of <code>CronJobInfo</code> instances.
   * @see org.sapia.corus.cron.CronJobInfo

   */
  public Results getCronJobs(ClusterInfo cluster);
  
  /**
   * Return the status of the process whose identifier is given..
   *
   * @param corusPid a process identifier.
   * @return a <code>ProcStatus</code> instance.
   */
  public ProcStatus getStatusFor(String corusPid) throws LogicException;

  /**
   * Returns the status for all processes.
   * 
   * @param cluster a <code>ClusterInfo</code> instance.
   */
  public Results getStatus(ClusterInfo cluster);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which process should have their status returned.
   * @param cluster a <code>ClusterInfo</code> instance.
   */
  public Results getStatus(String distName, ClusterInfo cluster);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return process status.
   * @param version the version of the distribution for which to return process status.
   * @param cluster a <code>ClusterInfo</code> instance.
   */
  public Results getStatus(String distName, String version, ClusterInfo cluster);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return process status.
   * @param version the version of the distribution for which to return process status.
   * @param profile the profile for which to return process status.
   * @param cluster a <code>ClusterInfo</code> instance.
   */
  public Results getStatus(String distName, String version, String profile, ClusterInfo cluster);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return process status.
   * @param version the version of the distribution for which to return process status.
   * @param profile the profile for which to return process status.
   * @param processName the name of the process for which to return process status.
   * @param cluster a <code>ClusterInfo</code> instance. 
   */
  public Results getStatus(String distName, String version, String profile,
                           String processName, ClusterInfo cluster);  
  
  /*////////////////////////////////////////////////////////////////////
                            PORT MANAGEMENT
  ////////////////////////////////////////////////////////////////////*/
  
  /**
   * @param name the name of the port range to add.
   * @param min the lowerbound port of the range.
   * @param max the higherbound port of the range.
   * @param cluster a <code>ClusterInfo</code> instance. 
   */
  public void addPortRange(String name, int min, int max, ClusterInfo cluster)
   throws PortRangeConflictException, PortRangeInvalidException;
  
  /**
   * @param name the name of the port range to add.
   * @param force if <code>true</code>, indicates that the port range should 
   * be removed even if the Corus server has corresponding ports flagged as
   * active.
   * @param cluster a <code>ClusterInfo</code> instance. 
   */
  public void removePortRange(String name, boolean force, ClusterInfo cluster)
    throws PortActiveException;  
  
  /**
   * Forces the releases of all ports of the given range.
   *
   * @param rangeName the name of an existing port range.
   * @param cluster a <code>ClusterInfo</code> instance. 
   */
  public void releasePortRange(String rangeName, ClusterInfo cluster);
  
  /**
   * Returns the <code>PortRange</code> instances that hold the pre-configured
   * ports of the specified Corus servers.
   *
   * @param cluster a <code>ClusterInfo</code> instance. 
   */
  public Results getPortRanges(ClusterInfo cluster);  
}
