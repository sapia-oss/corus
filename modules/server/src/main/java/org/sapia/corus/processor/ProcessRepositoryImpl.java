package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.Process;

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

  public synchronized ProcessDatabase getSuspendedProcesses() {
    return _suspended;
  }

  public synchronized ProcessDatabase getActiveProcesses() {
    return _active;
  }

  public synchronized ProcessDatabase getProcessesToRestart() {
    return _toRestart;
  }

  public synchronized int getProcessCountFor(ProcessRef processRef){
    return getActiveProcesses().getProcesses(
        processRef.getDist().getName(), 
        processRef.getDist().getVersion(),
        processRef.getProcessConfig().getName(), 
        processRef.getProfile()).size();
  }
  
  public synchronized int getProcessCountFor(String dist, String version, String processName, String profile){
    return getActiveProcesses().getProcesses(dist, version, processName, profile).size();
  }  

  public synchronized List<Process> getProcesses() {
    List<Process> procs = new ArrayList<Process>();
    procs.addAll(_active.getProcesses());
    procs.addAll(_suspended.getProcesses());
    procs.addAll(_toRestart.getProcesses());
    Collections.sort(procs);
    return procs;
  }
  
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
  
  public synchronized List<Process> getProcesses(Arg distName) {
    List<Process> procs = new ArrayList<Process>();
    procs.addAll(_active.getProcesses(distName));
    procs.addAll(_suspended.getProcesses(distName));
    procs.addAll(_toRestart.getProcesses(distName));
    Collections.sort(procs);
    return procs;
  }  

  public synchronized List<Process> getProcesses(Arg distName, Arg version) {
    List<Process> procs = new ArrayList<Process>();
    procs.addAll(_active.getProcesses(distName, version));
    procs.addAll(_suspended.getProcesses(distName, version));
    procs.addAll(_toRestart.getProcesses(distName, version));
    Collections.sort(procs);
    return procs;
  }

  public synchronized List<Process> getProcesses(Arg distName, Arg version,
                                        String profile) {
    List<Process> procs = new ArrayList<Process>();
    procs.addAll(_active.getProcesses(distName, version, profile));
    procs.addAll(_suspended.getProcesses(distName, version, profile));
    procs.addAll(_toRestart.getProcesses(distName, version, profile));
    Collections.sort(procs);
    return procs;
  }

  public synchronized List<Process> getProcesses(Arg distName, Arg version,
                                        String profile, Arg procName) {
    List<Process> procs = new ArrayList<Process>();
    procs.addAll(_active.getProcesses(distName, version, profile, procName));
    procs.addAll(_suspended.getProcesses(distName, version, profile, procName));
    procs.addAll(_toRestart.getProcesses(distName, version, profile, procName));
    Collections.sort(procs);
    return procs;
  }
}
