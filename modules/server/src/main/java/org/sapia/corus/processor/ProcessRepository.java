package org.sapia.corus.processor;

import java.util.List;

import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Specifies the behavior for storing/retrieving processes.
 * 
 * @author yduchesne
 * 
 */
public interface ProcessRepository {

  /**
   * @return the {@link ProcessDatabase} that holds suspended processes.
   */
  public ProcessDatabase getSuspendedProcesses();

  /**
   * @return the {@link ProcessDatabase} that holds active processes.
   */
  public ProcessDatabase getActiveProcesses();

  /**
   * @return the {@link ProcessDatabase} that holds processes to restart.
   */
  public ProcessDatabase getProcessesToRestart();

  /**
   * @param criteria
   *          a {@link ProcessCriteria}
   * @return the number of active processes matching the given parameters.
   */
  public int getActiveProcessCountFor(ProcessCriteria criteria);

  /**
   * @return the {@link List} of {@link Process} instances that this instance
   *         contains, whatever their status.
   * 
   * @see Process
   */
  public List<Process> getProcesses();

  /**
   * Returns the process that corresponds to the given identifier.
   * 
   * @param corusPid
   *          a process identifier.
   * @return the {@link Process} with the given identifier
   * @throws ProcessNotFoundException
   *           if no process object could be found for the given identifier.
   */
  public Process getProcess(String corusPid) throws ProcessNotFoundException;

  /**
   * @return the {@link List} of {@link Process} instances that match the given
   *         criteria.
   * 
   * @param a
   *          {@link ProcessCriteria}.
   * 
   * @see Process
   */
  public List<Process> getProcesses(ProcessCriteria criteria);

}