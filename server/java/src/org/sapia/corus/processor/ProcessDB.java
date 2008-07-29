package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.LogicException;
import org.sapia.corus.admin.CommandArg;


/**
 * An instance of this class holds the <code>ProcessStore</code>s that
 * contain <code>Process</code>es in different states.
 *
 * @see org.sapia.corus.processor.Process
 * @see org.sapia.corus.processor.ProcessStore
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ProcessDB {
  private ProcessStore _suspended;
  private ProcessStore _active;
  private ProcessStore _toRestart;

  /**
   * @param suspended the <code>ProcessStore</code> that will hold <code>Process</code> instances corresponding
   * to suspended processes.
   * @param active the <code>ProcessStore</code> that will hold <code>Process</code> instances corresponding
   * to active processes.
   * @param toRestart the <code>ProcessStore</code> that will hold <code>Process</code> instances corresponding
   * to processes in restart mode.
   */
  public ProcessDB(ProcessStore suspended, ProcessStore active,
                   ProcessStore toRestart) {
    _suspended = suspended;
    _active    = active;
    _toRestart = toRestart;
  }

  /**
   * @return the <code>ProcessStore</code> that holds suspended processes.
   */
  public synchronized ProcessStore getSuspendedProcesses() {
    return _suspended;
  }

  /**
   * @return the <code>ProcessStore</code> that holds active processes.
   */
  public synchronized ProcessStore getActiveProcesses() {
    return _active;
  }

  /**
   * @return the <code>ProcessStore</code> that holds processes to restart.
   */
  public synchronized ProcessStore getProcessesToRestart() {
    return _toRestart;
  }

  /**
   * @return the <code>List</code> of <code>Process</code> instances that
   * this instance contains, whatever their status.
   *
   * @see Process
   */
  public synchronized List getProcesses() {
    List procs = new ArrayList();
    procs.addAll(_active.getProcesses());
    procs.addAll(_suspended.getProcesses());
    procs.addAll(_toRestart.getProcesses());

    return procs;
  }
  
  /**
   * Returns the process that corresponds to the given identifier.
   * 
   * @param corusPid a process identifier.
   * @return a <code>Process</code>
   * @throws LogicException if no process object could be found for the
   * given identifier.
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
  
  /**
   * @return the <code>List</code> of <code>Process</code> instances that
   * this instance contains for the given distribution, and whatever their status.
   *
   * @param distName the name of the distribution that the returned processes
   * belong to.
   *
   * @see Process
   */
  public synchronized List getProcesses(CommandArg distName) {
    List procs = new ArrayList();
    procs.addAll(_active.getProcesses(distName));
    procs.addAll(_suspended.getProcesses(distName));
    procs.addAll(_toRestart.getProcesses(distName));
    return procs;
  }  

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
  public synchronized List getProcesses(CommandArg distName, CommandArg version) {
    List procs = new ArrayList();
    procs.addAll(_active.getProcesses(distName, version));
    procs.addAll(_suspended.getProcesses(distName, version));
    procs.addAll(_toRestart.getProcesses(distName, version));

    return procs;
  }

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
  public synchronized List getProcesses(CommandArg distName, CommandArg version,
                                        String profile) {
    List procs = new ArrayList();
    procs.addAll(_active.getProcesses(distName, version, profile));
    procs.addAll(_suspended.getProcesses(distName, version, profile));
    procs.addAll(_toRestart.getProcesses(distName, version, profile));

    return procs;
  }

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
  public synchronized List getProcesses(CommandArg distName, CommandArg version,
                                        String profile, CommandArg procName) {
    List procs = new ArrayList();
    procs.addAll(_active.getProcesses(distName, version, profile, procName));
    procs.addAll(_suspended.getProcesses(distName, version, profile, procName));
    procs.addAll(_toRestart.getProcesses(distName, version, profile, procName));

    return procs;
  }
}
