package org.sapia.corus.processor;

import java.util.List;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.Process;

public interface ProcessDatabase {

  /**
   * @param process a <code>Process</code>.
   */
  public abstract void addProcess(Process process);
  
  /**
   * @param corusPid a process identifier.
   * @return <code>true</code> if this instance contains the given
   * identifier.
   */
  public abstract boolean containsProcess(String corusPid);

  /**
   * Removes all processes corresponding to the given parameters.
   * 
   * @param name the name of the distribution from which to remove processes.
   * @param version the version of the distribution from which to remove processes. 
   */
  public abstract void removeProcesses(Arg name, Arg version);

  /**
   * @return the {@link List} of {@link Process}es held by
   * this instance.
   */
  public abstract List<Process> getProcesses();

  /**
   * Returns the list of processes corresponding to the distribution
   * whose name is given.
   * 
   * @param name a distribution name.
   * @return a {@link List} of processes.
   */
  public abstract List<Process> getProcesses(Arg name);

  /**
   * Returns the list of processes corresponding to the distribution
   * whose name and version are given.
   * 
   * @param name a distribution name.
   * @param version a distribution version. 
   * @return a {@link List} of {@link Process}es.
   */
  public abstract List<Process> getProcesses(Arg name, Arg version);

  /**
   * @param name a distribution name
   * @param version a distribution version.
   * @param processName a process name.
   * @param profile a profile.
   * @return a {@link List} of {@link Process}es.
   */
  public abstract List<Process> getProcesses(String name, String version,
      String processName, String profile);

  /**
   * Returns the list of processes corresponding to the distribution
   * whose name, version and profile are given.
   * 
   * @param name an {@link Arg} corresponding to a distribution name.
   * @param version an {@link Arg} corresponding to distribution version. 
   * @param profile a profile name. 
   * @return a {@link List} of {@link Process}es.
   */
  public abstract List<Process> getProcesses(Arg name,
      Arg version, String profile);

  /**
   * Returns the list of processes corresponding to the distribution
   * whose name, version, profile and process config name are given.
   * 
   * @param name an {@link Arg} corresponding to a distribution name.
   * @param version an {@link Arg} corresponding to distribution version. 
   * @param profile a profile name.
   * @param processName an {@link Arg} corresponding to a process name. 
   * @return a {@link List} of {@link Process}es.

   */
  public abstract List<Process> getProcesses(Arg name,
      Arg version, String profile, Arg processName);

  /**
   * Removes the process with the given ID.
   * 
   * @param corusPid a Corus process ID.
   */
  public abstract void removeProcess(String corusPid);

  /**
   * @param corusPid a Corus process ID.
   * @return the {@link Process} corresponding to the given ID.
   * @throws ProcessNotFoundException if no process with the given ID could
   * be found.
   */
  public abstract Process getProcess(String corusPid) throws ProcessNotFoundException;

}