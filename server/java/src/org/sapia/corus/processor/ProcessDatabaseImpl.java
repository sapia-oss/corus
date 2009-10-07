package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.db.DbMap;
import org.sapia.corus.exceptions.LogicException;


/**
 * Holds <code>Process</code> instances.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ProcessDatabaseImpl implements ProcessDatabase {
  private DbMap<String, Process> _processes;

  /**
   * @param map a <code>DbMap</code>.
   */
  public ProcessDatabaseImpl(DbMap<String, Process> map) {
    _processes = map;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#addProcess(org.sapia.corus.admin.services.processor.Process)
   */
  public synchronized void addProcess(Process process) {
    _processes.put(process.getProcessID(), process);
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#containsProcess(java.lang.String)
   */
  public synchronized boolean containsProcess(String corusPid){
    return _processes.get(corusPid) != null;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#removeProcesses(org.sapia.corus.admin.CommandArg, org.sapia.corus.admin.CommandArg)
   */
  public synchronized void removeProcesses(Arg name, Arg version) {
    Process  current;
    Iterator<Process> processes = _processes.values();
    List<Process>     toRemove  = new ArrayList<Process>();

    for (; processes.hasNext();) {
      current = (Process) processes.next();

      if (name.matches(current.getDistributionInfo().getName()) &&
            version.matches(current.getDistributionInfo().getVersion())) {
        toRemove.add(current);
      }
    }

    for (int i = 0; i < toRemove.size(); i++) {
      _processes.remove(((Process) toRemove.get(i)).getProcessID());
    }
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcesses()
   */
  public synchronized List<Process> getProcesses() {
    List<Process>     toReturn  = new ArrayList<Process>();
    Iterator<Process> processes = _processes.values();

    for (; processes.hasNext();) {
      toReturn.add((Process) processes.next());
    }

    return toReturn;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcesses(org.sapia.corus.admin.CommandArg)
   */
  public synchronized List<Process> getProcesses(Arg name) {
    List<Process> toReturn  = new ArrayList<Process>();
    Process  current;
    Iterator<Process> processes = _processes.values();

    for (; processes.hasNext();) {
      current = (Process) processes.next();

      if (name.matches(current.getDistributionInfo().getName())) {
        toReturn.add(current);
      }
    }

    return toReturn;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcesses(org.sapia.corus.admin.CommandArg, org.sapia.corus.admin.CommandArg)
   */  
  public synchronized List<Process> getProcesses(Arg name, Arg version) {
    List<Process>     toReturn  = new ArrayList<Process>();
    Process  current;
    Iterator<Process> processes = _processes.values();

    for (; processes.hasNext();) {
      current = (Process) processes.next();

      if (name.matches(current.getDistributionInfo().getName()) &&
            version.matches(current.getDistributionInfo().getVersion())) {
        toReturn.add(current);
      }
    }

    return toReturn;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcesses(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  public synchronized List<Process> getProcesses(String name, String version, String processName, String profile) {
    List<Process>     toReturn  = new ArrayList<Process>();
    Process  current;
    Iterator<Process> processes = _processes.values();

    for (; processes.hasNext();) {
      current = (Process) processes.next();

      if (name.equals(current.getDistributionInfo().getName()) &&
          version.equals(current.getDistributionInfo().getVersion()) &&
          profile.equals(current.getDistributionInfo().getProfile()) &&
          processName.equals(current.getDistributionInfo().getProcessName())) {
        toReturn.add(current);
      }
    }

    return toReturn;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcesses(org.sapia.corus.admin.CommandArg, org.sapia.corus.admin.CommandArg, java.lang.String)
   */    
  public synchronized List<Process> getProcesses(Arg name, Arg version, String profile) {
    List<Process>     toReturn  = new ArrayList<Process>();
    Process  current;
    Iterator<Process> processes = _processes.values();

    for (; processes.hasNext();) {
      current = (Process) processes.next();

      if (name.matches(current.getDistributionInfo().getName()) &&
          version.matches(current.getDistributionInfo().getVersion()) &&
            (profile == null || current.getDistributionInfo().getProfile().equals(profile))) {
        toReturn.add(current);
      }
    }

    return toReturn;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcesses(org.sapia.corus.admin.CommandArg, org.sapia.corus.admin.CommandArg, java.lang.String, org.sapia.corus.admin.CommandArg)
   */    
  public synchronized List<Process> getProcesses(Arg name, Arg version, String profile,
                                 Arg processName) {
    List<Process>     toReturn  = new ArrayList<Process>();
    Process  current;
    Iterator<Process> processes = _processes.values();

    for (; processes.hasNext();) {
      current = (Process) processes.next();
      if (name.matches(current.getDistributionInfo().getName()) &&
          version.matches(current.getDistributionInfo().getVersion()) &&
          (profile == null || current.getDistributionInfo().getProfile().equals(profile)) &&
          processName.matches(current.getDistributionInfo().getProcessName())) {
        toReturn.add(current);
      }
    }

    return toReturn;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#removeProcess(java.lang.String)
   */
  public synchronized void removeProcess(String corusPid) {
    _processes.remove(corusPid);
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.processor.ProcessDatabase#getProcess(java.lang.String)
   */
  public synchronized Process getProcess(String corusPid) throws LogicException {
    Process current = (Process) _processes.get(corusPid);

    if (current == null) {
      throw new LogicException("No process for ID: " + corusPid);
    }

    return current;
  }
}
