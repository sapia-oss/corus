package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * An instance of this class holds the {@link ProcessDatabase}s that contain
 * {@link Process}es in different states.
 * 
 * @author Yanick Duchesne
 * 
 */
public class ProcessRepositoryImpl implements ProcessRepository {

  private ProcessDatabase suspended;
  private ProcessDatabase active;
  private ProcessDatabase toRestart;

  /**
   * @param suspended
   *          the {@link ProcessDatabase} that will hold {@link Process}
   *          instances corresponding to suspended processes.
   * @param active
   *          the {@link ProcessDatabase} that will hold {@link Process}
   *          instances corresponding to active processes.
   * @param toRestart
   *          the {@link ProcessDatabase} that will hold {@link Process}
   *          instances corresponding to processes in restart mode.
   */
  public ProcessRepositoryImpl(ProcessDatabase suspended, ProcessDatabase active, ProcessDatabase toRestart) {
    this.suspended = suspended;
    this.active = active;
    this.toRestart = toRestart;
  }

  @Override
  public synchronized ProcessDatabase getSuspendedProcesses() {
    return suspended;
  }

  @Override
  public synchronized ProcessDatabase getActiveProcesses() {
    return active;
  }

  @Override
  public synchronized ProcessDatabase getProcessesToRestart() {
    return toRestart;
  }

  @Override
  public synchronized int getActiveProcessCountFor(ProcessCriteria criteria) {
    return getActiveProcesses().getProcesses(criteria).size();
  }

  @Override
  public synchronized List<Process> getProcesses() {
    List<Process> procs = new ArrayList<Process>();
    ProcessCriteria criteria = ProcessCriteria.builder().all();
    procs.addAll(active.getProcesses(criteria));
    procs.addAll(suspended.getProcesses(criteria));
    procs.addAll(toRestart.getProcesses(criteria));
    Collections.sort(procs);
    return procs;
  }

  @Override
  public synchronized Process getProcess(String corusPid) throws ProcessNotFoundException {
    if (active.containsProcess(corusPid)) {
      return active.getProcess(corusPid);
    } else if (suspended.containsProcess(corusPid)) {
      return suspended.getProcess(corusPid);
    } else if (toRestart.containsProcess(corusPid)) {
      return toRestart.getProcess(corusPid);
    }
    throw new ProcessNotFoundException("No process found for ID: " + corusPid);
  }

  @Override
  public synchronized List<Process> getProcesses(ProcessCriteria criteria) {
    List<Process> procs = new ArrayList<Process>();
    procs.addAll(active.getProcesses(criteria));
    procs.addAll(suspended.getProcesses(criteria));
    procs.addAll(toRestart.getProcesses(criteria));
    Collections.sort(procs);
    return procs;
  }
}
