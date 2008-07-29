package org.sapia.corus.processor;

import org.sapia.corus.LogicException;
import org.sapia.corus.admin.CommandArg;
import org.sapia.corus.db.DbMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
public class ProcessStore {
  private DbMap _processes;

  /**
   * @param map a <code>DbMap</code>.
   */
  public ProcessStore(DbMap map) {
    _processes = map;
  }
  
  /**
   * @param process a <code>Process</code>.
   */
  public synchronized void addProcess(Process process) {
    _processes.put(process.getProcessID(), process);
  }
  
  /**
   * @param corusPid a process identifier.
   * @return <code>true</code> if this instance contains the given
   * identifier.
   */
  public synchronized boolean containsProcess(String corusPid){
    return _processes.get(corusPid) != null;
  }

  /**
   * Removes all processes corresponding to the given parameters.
   * 
   * @param name the name of the distribution from which to remove processes.
   * @param version the version of the distribution from which to remove processes. 
   */
  public synchronized void removeProcesses(CommandArg name, CommandArg version) {
    Process  current;
    Iterator processes = _processes.values();
    List     toRemove  = new ArrayList();

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

  /**
   * @return the <code>List</code> of <code>Process</code>es held by
   * this instance.
   */
  public synchronized List getProcesses() {
    List     toReturn  = new ArrayList();
    Iterator processes = _processes.values();

    for (; processes.hasNext();) {
      toReturn.add((Process) processes.next());
    }

    return toReturn;
  }
  
  /**
   * Returns the list of processes corresponding to the distribution
   * whose name is given.
   * 
   * @param name a distribution name.
   * @return a <code>List</code> of processes.
   */
  public synchronized List getProcesses(CommandArg name) {
    List     toReturn  = new ArrayList();
    Process  current;
    Iterator processes = _processes.values();

    for (; processes.hasNext();) {
      current = (Process) processes.next();

      if (name.matches(current.getDistributionInfo().getName())) {
        toReturn.add(current);
      }
    }

    return toReturn;
  }

  /**
   * Returns the list of processes corresponding to the distribution
   * whose name and version are given.
   * 
   * @param name a distribution name.
   * @param version a distribution version. 
   * @return a <code>List</code> of processes.
   */  
  public synchronized List getProcesses(CommandArg name, CommandArg version) {
    List     toReturn  = new ArrayList();
    Process  current;
    Iterator processes = _processes.values();

    for (; processes.hasNext();) {
      current = (Process) processes.next();

      if (name.matches(current.getDistributionInfo().getName()) &&
            version.matches(current.getDistributionInfo().getVersion())) {
        toReturn.add(current);
      }
    }

    return toReturn;
  }

  /**
   * Returns the list of processes corresponding to the distribution
   * whose name, version and profile are given.
   * 
   * @param name a distribution name.
   * @param version a distribution version. 
   * @param profile a distribution . 
   * @return a <code>List</code> of processes.
   */    
  public synchronized List getProcesses(CommandArg name, CommandArg version, String profile) {
    List     toReturn  = new ArrayList();
    Process  current;
    Iterator processes = _processes.values();

    for (; processes.hasNext();) {
      current = (Process) processes.next();

      if (name.matches(current.getDistributionInfo().getName()) &&
          version.matches(current.getDistributionInfo().getVersion()) &&
            current.getDistributionInfo().getProfile().equals(profile)) {
        toReturn.add(current);
      }
    }

    return toReturn;
  }

  /**
   * Returns the list of processes corresponding to the distribution
   * whose name, version, profile and process config name are given.
   * 
   * @param name a distribution name.
   * @param version a distribution version. 
   * @param profile a distribution . 
   * @param processName a process config name.
   * @return a <code>List</code> of processes.
   */    
  public synchronized List getProcesses(CommandArg name, CommandArg version, String profile,
                                 CommandArg processName) {
    List     toReturn  = new ArrayList();
    Process  current;
    Iterator processes = _processes.values();

    for (; processes.hasNext();) {
      current = (Process) processes.next();
      if(profile != null){
        if (name.matches(current.getDistributionInfo().getName()) &&
            version.matches(current.getDistributionInfo().getVersion()) &&
            current.getDistributionInfo().getProfile().equals(profile) &&
            processName.matches(current.getDistributionInfo().getProcessName())) {
          toReturn.add(current);
        }
        
      }
      else{
        if (name.matches(current.getDistributionInfo().getName()) &&
            version.matches(current.getDistributionInfo().getVersion()) &&
            processName.matches(current.getDistributionInfo().getProcessName())) {
          toReturn.add(current);
        }
      }
    }

    return toReturn;
  }

  public synchronized void removeProcess(String corusPid) {
    _processes.remove(corusPid);
  }

  public synchronized Process getProcess(String corusPid) throws LogicException {
    Process current = (Process) _processes.get(corusPid);

    if (current == null) {
      throw new LogicException("No process for ID: " + corusPid);
    }

    return current;
  }
}
