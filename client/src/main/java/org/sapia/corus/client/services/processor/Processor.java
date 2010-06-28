package org.sapia.corus.client.services.processor;

import java.util.List;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.interop.Status;


/**
 * This interface specifies remote process administration behavior.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface Processor extends java.rmi.Remote, Module {
  String ROLE = Processor.class.getName();

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
   *
   * @return a <code>ProgressQueue</code>.
   */
  public ProgressQueue exec(Arg distName, Arg version, String profile,
                            int instances);

  /**
   * Starts process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to start
   * new process(es).
   * @param version the version of the distribution for which to start
   * new process(es).
   * @param profile the name of the profile under which the new process(es)
   * should be started.
   * @param processName the name of the process configuration for which new process(es)
   * should be started.
   * @param the number of process(es) to start.
   *
   * @return a <code>ProgressQueue</code>.
   */
  public ProgressQueue exec(Arg distName, Arg version, String profile,
                            Arg processName, int instances);

  /**
   * 
   * @param execConfigName the name of the execution configuration from which to start
   * processes.
   * @return a {@link ProgressQueue}
   */
  public ProgressQueue exec(String execConfigName);

  /**
   * Resumes all suspended processes.
   *
   * @return a <code>ProgressQueue</code>.
   */
  public ProgressQueue resume();

  /**
   * Shuts down and restarts the process with the given ID.
   *
   * @param a corus pid..
   */
  public void restartByAdmin(String pid) throws ProcessNotFoundException;
  
  /**
   * Shuts down and restarts the process with the given ID.
   * @see Processor#restart(String)
   */
  public void restart(String pid) throws ProcessNotFoundException;

  /**
   * Kill the process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to kill
   * running processes.
   * @param version the version of the distribution for which to kill
   * running processes.
   * @param profile the name of the profile for which to kill the running process(es).
   *
   * @return a <code>ProgressQueue</code>.
   */
  public void kill(Arg distName, Arg version, String profile,
                   boolean suspend);

  /**
   * Kill the process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to kill
   * running VMs.
   * @param version the version of the distribution for which to kill
   * running VMs.
   * @param profile the name of the profile for which to kill the running process(es).
   * @param processName the name of the process configuration for which to kill the running process(es).
   *
   * @return a <code>ProgressQueue</code>.
   */
  public void kill(Arg distName, Arg version, String profile,
                   Arg processName, boolean suspend);

  /**
   * Kills the process with the given identifier.
   *
   * @param a process identifier.
   */
  public void kill(String corusPid, boolean suspend) throws ProcessNotFoundException;

  /**
   * Return the process whose identifier is given..
   *
   * @param corusPid a process identifier.
   * @return a <code>Process</code> instance.
   */
  public Process getProcess(String corusPid) throws ProcessNotFoundException;

  /**
   * Returns all process objects.
   *
   * @return a <code>List</code> of <code>Process</code> instances.
   */
  public List<Process> getProcesses();

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return processes.
   * @return a <code>List</code> of <code>Process</code> instances.
   */
  public List<Process> getProcesses(Arg distName);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return processes.
   * @param version the version of the distribution for which to return processes.
   * @return a <code>List</code> of <code>Process</code> instances.
   */
  public List<Process> getProcesses(Arg distName, Arg version);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return processes.
   * @param version the version of the distribution for which to return processes.
   * @param profile the profile for which to return processes.
   * @return a <code>List</code> of <code>Process</code> instances.
   */
  public List<Process> getProcesses(Arg distName, Arg version, String profile);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return processes.
   * @param version the version of the distribution for which to return processes.
   * @param profile the profile for which to return VM processes.
   * @param processName the name of the process for which to return process instances.
   * @return a <code>List</code> of <code>Process</code> instances.
   */
  public List<Process> getProcesses(Arg distName, Arg version, String profile,
      Arg processName);
  
  /**
   * Return the status of the process whose identifier is given..
   *
   * @param corusPid a process identifier.
   * @return a <code>Status</code> instance.
   */
  public ProcStatus getStatusFor(String corusPid) throws ProcessNotFoundException;

  /**
   * Returns the status for all processes.
   *
   * @return a <code>List</code> of <code>Status</code> instances.
   */
  public List<Status> getStatus();

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which process should have their status returned.
   * @return a <code>List</code> of <code>Status</code> instances.
   */
  public List<Status> getStatus(Arg distName);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return process status.
   * @param version the version of the distribution for which to return process status.
   * @return a <code>List</code> of <code>Status</code> instances.
   */
  public List<Status> getStatus(Arg distName, Arg version);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return process status.
   * @param version the version of the distribution for which to return process status.
   * @param profile the profile for which to return process status.
   * @return a <code>List</code> of <code>Status</code> instances.
   */
  public List<Status> getStatus(Arg distName, Arg version, String profile);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return process status.
   * @param version the version of the distribution for which to return process status.
   * @param profile the profile for which to return process status.
   * @param processName the name of the process for which to return process status.
   * @return a <code>List</code> of <code>Status</code> instances.
   */
  public List<Status> getStatus(Arg distName, Arg version, String profile,
      Arg processName);
  
  /**
   * Returns all processes that have acquired one or more ports.
   *
   * @return a <code>List</code> of <code>Process</code> instances.
   */  
  public List<Process> getProcessesWithPorts();
  
 
  /**
   * @param conf adds an {@link ExecConfig} to this instance.
   */
  public void addExecConfig(ExecConfig conf);
  
  /**
   * @return the list of {@link ExecConfig}s that are contained in this instance.
   */
  public List<ExecConfig> getExecConfigs();
  
  /**
   * @param name removes the {@link ExecConfig} whose name matches the given argument.
   */
  public void removeExecConfig(Arg name);
  
  /**
   * @return this instance'S {@link ProcessorConfigurationImpl}
   */
  public ProcessorConfiguration getConfiguration();
}
