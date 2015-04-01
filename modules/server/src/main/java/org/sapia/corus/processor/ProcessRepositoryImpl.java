package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.ubik.util.Strings;

/**
 * An instance of this class holds the {@link ProcessDatabase}s that contain
 * {@link Process}es in different states.
 * 
 * @author Yanick Duchesne
 * 
 */
public class ProcessRepositoryImpl implements ProcessRepository {

  private ProcessDatabase processDb;

  /**
   * @param processDb
   *          the {@link ProcessDatabase} that manage persistence of the {@link Process}
   *          instances that this instance manipulates.
   */
  public ProcessRepositoryImpl(ProcessDatabase processDb) {
    this.processDb = processDb;
  }

  @Override
  public synchronized List<Process> getSuspendedProcesses() {
    return processDb.getProcesses(ProcessCriteria.builder().lifecycles(
        LifeCycleStatus.SUSPENDED)
    .build());
  }

  @Override
  public synchronized List<Process> getActiveProcesses() {
    return processDb.getProcesses(ProcessCriteria.builder().lifecycles(
        LifeCycleStatus.ACTIVE,
        LifeCycleStatus.STALE
    ).build());
  }

  @Override
  public synchronized List<Process> getProcessesToRestart() {
    return processDb.getProcesses(ProcessCriteria.builder().lifecycles(
        LifeCycleStatus.RESTARTING
    ).build());
  }

  @Override
  public synchronized int getActiveProcessCountFor(ProcessCriteria criteria) {
    return getActiveProcesses().size();
  }

  @Override
  public synchronized List<Process> getProcesses() {
    List<Process> procs = new ArrayList<Process>();
    ProcessCriteria criteria = ProcessCriteria.builder().all();
    procs.addAll(processDb.getProcesses(criteria));
    Collections.sort(procs);
    return procs;
  }

  @Override
  public synchronized Process getProcess(String corusPid) throws ProcessNotFoundException {
    Process toReturn = processDb.getProcess(corusPid);
    if (toReturn == null) {
      throw new ProcessNotFoundException("No process found for ID: " + corusPid);
    }
    return toReturn;
  }

  @Override
  public synchronized List<Process> getProcesses(ProcessCriteria criteria) {
    List<Process> procs = new ArrayList<Process>();
    procs.addAll(processDb.getProcesses(criteria));
    Collections.sort(procs);
    return procs;
  }
  
  @Override
  public synchronized void removeProcess(String corusPid) {
    processDb.removeProcess(corusPid);
  }
  
  @Override
  public synchronized void addProcess(Process proc) {
    processDb.addProcess(proc);
  }
  
  @Override
  public synchronized boolean containsProcess(String corusPid) {
    return processDb.containsProcess(corusPid);
  }
  
  @Override
  public synchronized void dump(JsonStream stream) {
    processDb.dump(stream);
  }
  
  @Override
  public synchronized void load(JsonInput dump) {
    processDb.load(dump);
  }
  
  @Override
  public String toString() {
    return Strings.toString("processDb", processDb);
  }
}
