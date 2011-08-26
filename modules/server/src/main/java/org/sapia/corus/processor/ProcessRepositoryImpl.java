package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * An instance of this class holds the {@link ProcessDatabase}s that
 * contain <code>Process</code>es in different states.
 *
 * @author Yanick Duchesne
 *
 */
public class ProcessRepositoryImpl implements ProcessRepository {
  private ProcessDatabase _suspended;
  private ProcessDatabase _active;
  private ProcessDatabase _toRestart;

  /**
   * @param suspended the <code>ProcessStore</code> that will hold <code>Process</code> instances corresponding
   * to suspended processes.
   * @param active the <code>ProcessStore</code> that will hold <code>Process</code> instances corresponding
   * to active processes.
   * @param toRestart the <code>ProcessStore</code> that will hold <code>Process</code> instances corresponding
   * to processes in restart mode.
   */
  public ProcessRepositoryImpl(ProcessDatabase suspended, ProcessDatabase active,
                   ProcessDatabase toRestart) {
    _suspended = suspended;
    _active    = active;
    _toRestart = toRestart;
  }

  @Override
  public synchronized ProcessDatabase getSuspendedProcesses() {
    return _suspended;
  }

  @Override
  public synchronized ProcessDatabase getActiveProcesses() {
    return _active;
  }

  @Override
  public synchronized ProcessDatabase getProcessesToRestart() {
    return _toRestart;
  }

  @Override
  public synchronized int getActiveProcessCountFor(ProcessCriteria criteria){
    return getActiveProcesses().getProcesses(criteria).size();
  }  

  @Override
  public synchronized List<Process> getProcesses() {
    List<Process> procs = new ArrayList<Process>();
    ProcessCriteria criteria = ProcessCriteria.builder().all();
    procs.addAll(_active.getProcesses(criteria));
    procs.addAll(_suspended.getProcesses(criteria));
    procs.addAll(_toRestart.getProcesses(criteria));
    Collections.sort(procs);
    return procs;
  }
  
  @Override
  public synchronized Process getProcess(String corusPid) throws ProcessNotFoundException{
    if(_active.containsProcess(corusPid)){
      return _active.getProcess(corusPid);
    }
    else if(_suspended.containsProcess(corusPid)){
      return _suspended.getProcess(corusPid);
    }
    else if(_toRestart.containsProcess(corusPid)){
      return _toRestart.getProcess(corusPid);
    }
    throw new ProcessNotFoundException("No process found for ID: " + corusPid);
  }
  
  @Override
  public synchronized List<Process> getProcesses(ProcessCriteria criteria) {
    List<Process> procs = new ArrayList<Process>();
    procs.addAll(_active.getProcesses(criteria));
    procs.addAll(_suspended.getProcesses(criteria));
    procs.addAll(_toRestart.getProcesses(criteria));
    Collections.sort(procs);
    return procs;
  }  
}
