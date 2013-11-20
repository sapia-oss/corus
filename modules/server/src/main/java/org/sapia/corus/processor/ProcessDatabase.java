package org.sapia.corus.processor;

import java.util.List;

import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Specifies the behavior for storing/retrieving {@link Process} instances.
 * 
 * @author yduchesne
 * 
 */
public interface ProcessDatabase {

  /**
   * @param process
   *          a {@link Process}.
   */
  public void addProcess(Process process);

  /**
   * @param corusPid
   *          a process identifier.
   * @return <code>true</code> if this instance contains the given identifier.
   */
  public boolean containsProcess(String corusPid);

  /**
   * Removes all processes corresponding to the given criteria.
   * 
   * @param criteria
   *          a {@link ProcessCriteria}
   */
  public void removeProcesses(ProcessCriteria criteria);

  /**
   * @param criteria
   *          a {@link ProcessCriteria}
   * @return the {@link List} of {@link Process}es corresponding to the given
   *         criteria.
   */
  public List<Process> getProcesses(ProcessCriteria criteria);

  /**
   * Removes the process with the given ID.
   * 
   * @param corusPid
   *          a Corus process ID.
   */
  public void removeProcess(String corusPid);

  /**
   * @param corusPid
   *          a Corus process ID.
   * @return the {@link Process} corresponding to the given ID.
   * @throws ProcessNotFoundException
   *           if no process with the given ID could be found.
   */
  public Process getProcess(String corusPid) throws ProcessNotFoundException;

}