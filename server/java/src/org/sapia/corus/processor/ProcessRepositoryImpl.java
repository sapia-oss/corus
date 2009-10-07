package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.exceptions.LogicException;

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

  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getSuspendedProcesses()
   */
  public synchronized ProcessDatabase getSuspendedProcesses() {
    return _suspended;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getActiveProcesses()
   */
  public synchronized ProcessDatabase getActiveProcesses() {
    return _active;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcessesToRestart()
   */
  public synchronized ProcessDatabase getProcessesToRestart() {
    return _toRestart;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcessCountFor(org.sapia.corus.processor.ProcessRef)
   */
  public synchronized int getProcessCountFor(ProcessRef processRef){
    return getActiveProcesses().getProcesses(
        processRef.getDist().getName(), 
        processRef.getDist().getVersion(),
        processRef.getProcessConfig().getName(), 
        processRef.getProfile()).size();
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcesses()
   */
  public synchronized List<Process> getProcesses() {
    List<Process> procs = new ArrayList<Process>();
    procs.addAll(_active.getProcesses());
    procs.addAll(_suspended.getProcesses());
    procs.addAll(_toRestart.getProcesses());

    return procs;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcess(java.lang.String)
   */
  public synchronized Process getProcess(String corusPid) throws LogicException{
    if(_active.containsProcess(corusPid)){
      return _active.getProcess(corusPid);
    }
    else if(_suspended.containsProcess(corusPid)){
      return _suspended.getProcess(corusPid);
    }
    else if(_toRestart.containsProcess(corusPid)){
      return _toRestart.getProcess(corusPid);
    }
    throw new LogicException("No process found for ID: " + corusPid);
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcesses(org.sapia.corus.admin.CommandArg)
   */
  public synchronized List<Process> getProcesses(Arg distName) {
    List<Process> procs = new ArrayList<Process>();
    procs.addAll(_active.getProcesses(distName));
    procs.addAll(_suspended.getProcesses(distName));
    procs.addAll(_toRestart.getProcesses(distName));
    return procs;
  }  

  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcesses(org.sapia.corus.admin.CommandArg, org.sapia.corus.admin.CommandArg)
   */
  public synchronized List<Process> getProcesses(Arg distName, Arg version) {
    List<Process> procs = new ArrayList<Process>();
    procs.addAll(_active.getProcesses(distName, version));
    procs.addAll(_suspended.getProcesses(distName, version));
    procs.addAll(_toRestart.getProcesses(distName, version));

    return procs;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcesses(org.sapia.corus.admin.CommandArg, org.sapia.corus.admin.CommandArg, java.lang.String)
   */
  public synchronized List<Process> getProcesses(Arg distName, Arg version,
                                        String profile) {
    List<Process> procs = new ArrayList<Process>();
    procs.addAll(_active.getProcesses(distName, version, profile));
    procs.addAll(_suspended.getProcesses(distName, version, profile));
    procs.addAll(_toRestart.getProcesses(distName, version, profile));

    return procs;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcesses(org.sapia.corus.admin.CommandArg, org.sapia.corus.admin.CommandArg, java.lang.String, org.sapia.corus.admin.CommandArg)
   */
  public synchronized List<Process> getProcesses(Arg distName, Arg version,
                                        String profile, Arg procName) {
    List<Process> procs = new ArrayList<Process>();
    procs.addAll(_active.getProcesses(distName, version, profile, procName));
    procs.addAll(_suspended.getProcesses(distName, version, profile, procName));
    procs.addAll(_toRestart.getProcesses(distName, version, profile, procName));

    return procs;
  }
}
