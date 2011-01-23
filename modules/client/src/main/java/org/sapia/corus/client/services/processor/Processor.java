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
   * @param instances the number of process(es) to start.
   *
   * @return a {@link ProgressQueue}
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
   * @param instances the number of process(es) to start.
   *
   * @return a {@link ProgressQueue}.
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
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue resume();

  /**
   * Shuts down and restarts the process with the given ID.
   *
   * @param pid a Corus pid.
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
   * @param suspend if <code>true</code>, indicates that the process should be suspended.
   */
  public void kill(Arg distName, Arg version, String profile,
                   boolean suspend);

  /**
   * Kill the process(es) corresponding to the passed in parameters.
   *
   * @param distName the name of the distribution for which to kill
   * running processes.
   * @param version the version of the distribution for which to kill
   * running processes.
   * @param profile the name of the profile for which to kill the running process(es).
   * @param processName the name of the process configuration for which to kill the running process(es).
   * @param suspend if <code>true</code>, indicates that the process should be suspended.
   */
  public void kill(Arg distName, Arg version, String profile,
                   Arg processName, boolean suspend);

  /**
   * Kills the process with the given identifier.
   *
   * @param corusPid a process identifier.
   * @param suspend if <code>true</code>, indicates that the process should be suspended.
   */
  public void kill(String corusPid, boolean suspend) throws ProcessNotFoundException;

  /**
   * Return the process whose identifier is given.
   *
   * @param corusPid a process identifier.
   * @return a {@link Process} instance.
   */
  public Process getProcess(String corusPid) throws ProcessNotFoundException;

  /**
   * Returns all process objects held by this instance.
   *
   * @return a {@link List} of {@link Process} instances.
   */
  public List<Process> getProcesses();

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return processes.
   * @return a {@link List} of {@link Process} instances.
   */
  public List<Process> getProcesses(Arg distName);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return processes.
   * @param version the version of the distribution for which to return processes.
   * @return a {@link List} of {@link Process} instances.
   */
  public List<Process> getProcesses(Arg distName, Arg version);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return processes.
   * @param version the version of the distribution for which to return processes.
   * @param profile the profile for which to return processes.
   * @return a {@link List} of {@link Process} instances.
   */
  public List<Process> getProcesses(Arg distName, Arg version, String profile);

  /**
   * Returns all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return processes.
   * @param version the version of the distribution for which to return processes.
   * @param profile the profile for which to return VM processes.
   * @param processName the name of the process for which to return process instances.
   * @return a {@link List} of {@link Process} instances.
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
   * @return a {@link List} of {@link Status} instances.
   */
  public List<Status> getStatus();

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which process should have their status returned.
   * @return a {@link List} of {@link Status} instances.
   */
  public List<Status> getStatus(Arg distName);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return process status.
   * @param version the version of the distribution for which to return process status.
   * @return a {@link List} of {@link Status} instances.
   */
  public List<Status> getStatus(Arg distName, Arg version);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return process status.
   * @param version the version of the distribution for which to return process status.
   * @param profile the profile for which to return process status.
   * @return a {@link List} of {@link Status} instances.
   */
  public List<Status> getStatus(Arg distName, Arg version, String profile);

  /**
   * Returns the status of all processes corresponding to the given parameters.
   *
   * @param distName the name of the distribution for which to return process status.
   * @param version the version of the distribution for which to return process status.
   * @param profile the profile for which to return process status.
   * @param processName the name of the process for which to return process status.
   * @return a {@link List} of {@link Status} instances.
   */
  public List<Status> getStatus(Arg distName, Arg version, String profile,
      Arg processName);
  
  /**
   * Returns all processes that have acquired one or more ports.
   *
   * @return a {@link List} of {@link Process} instances.
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
   * @return this instance's {@link ProcessorConfiguration}
   */
  public ProcessorConfiguration getConfiguration();
}
