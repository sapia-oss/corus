package org.sapia.corus.processor;

import java.util.List;

import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.exceptions.LogicException;

public interface ProcessRepository {

  /**
   * @return the {@link ProcessDatabase} that holds suspended processes.
   */
  public abstract ProcessDatabase getSuspendedProcesses();

  /**
   * @return the {@link ProcessDatabase} that holds active processes.
   */
  public abstract ProcessDatabase getActiveProcesses();

  /**
   * @return the {@link ProcessDatabase} that holds processes to restart.
   */
  public abstract ProcessDatabase getProcessesToRestart();

  /**
   * @param processRef a {@link ProcessRef}
   * @return the number of processes for the given process reference
   */
  public abstract int getProcessCountFor(ProcessRef processRef);

  /**
   * @return the <code>List</code> of <code>Process</code> instances that
   * this instance contains, whatever their status.
   *
   * @see Process
   */
  public abstract List<Process> getProcesses();

  /**
   * Returns the process that corresponds to the given identifier.
   * 
   * @param corusPid a process identifier.
   * @return a <code>Process</code>
   * @throws LogicException if no process object could be found for the
   * given identifier.
   */
  public abstract Process getProcess(String corusPid) throws LogicException;

  /**
   * @return the <code>List</code> of <code>Process</code> instances that
   * this instance contains for the given distribution, and whatever their status.
   *
   * @param distName the name of the distribution that the returned processes
   * belong to.
   *
   * @see Process
   */
  public abstract List<Process> getProcesses(Arg distName);

  /**
   * @return the <code>List</code> of <code>Process</code> instances that
   * this instance contains for the given distribution and version,
   * and whatever their status.
   *
   * @param distName the name of the distribution that the returned processes
   * belong to.
   * @param version the version of the distribution that the returned processes
   * belong to.
   *
   * @see Process
   */
  public abstract List<Process> getProcesses(Arg distName,
      Arg version);

  /**
   * @return the <code>List</code> of <code>Process</code> instances that
   * this instance contains for the given distribution version, and profile,
   * and whatever their status.
   *
   * @param distName the name of the distribution that the returned processes
   * belong to.
   * @param version the version of the distribution that the returned processes
   * belong to.
   * @param profile the profile under which the returned processes were started.
   *
   * @see Process
   */
  public abstract List<Process> getProcesses(Arg distName,
      Arg version, String profile);

  /**
   * @return the <code>List</code> of <code>Process</code> instances that
   * this instance contains for the given distribution version profile,
   * matching the given process name, and whatever their status.
   *
   * @param distName the name of the distribution that the returned processes
   * belong to.
   * @param version the version of the distribution that the returned processes
   * belong to.
   * @param profile the profile under which the returned processes were started.
   * @param procName a process name.
   *
   * @see Process
   */
  public abstract List<Process> getProcesses(Arg distName,
      Arg version, String profile, Arg procName);

}