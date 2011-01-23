package org.sapia.corus.processor;

import java.util.List;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.util.CompositeMatcher;
import org.sapia.corus.util.IteratorFilter;
import org.sapia.corus.util.Matcher;


/**
 * Holds {@link Process} instances.
 * 
 * @author Yanick Duchesne
 */
public class ProcessDatabaseImpl implements ProcessDatabase {
  private DbMap<String, Process> _processes;

  
  public ProcessDatabaseImpl(DbMap<String, Process> map) {
    _processes = map;
  }
  
  public synchronized void addProcess(Process process) {
    _processes.put(process.getProcessID(), process);
  }
  
  public synchronized boolean containsProcess(String corusPid){
    return _processes.get(corusPid) != null;
  }

  public synchronized void removeProcesses(Arg name, Arg version) {
    List<Process>     toRemove  = getProcesses(name, version);
    for (int i = 0; i < toRemove.size(); i++) {
      _processes.remove(((Process) toRemove.get(i)).getProcessID());
    }
  }

  public synchronized List<Process> getProcesses() {
    return new IteratorFilter<Process>(new CompositeMatcher<Process>()).filter(_processes.values()).sort(new ProcessComparator()).get();
  }
  
  public synchronized List<Process> getProcesses(final Arg name) {
    return getProcesses(replaceNull(name), replaceNull(null), null, replaceNull(null));
  }
 
  public synchronized List<Process> getProcesses(Arg name, Arg version) {
    return getProcesses(replaceNull(name), replaceNull(version), null, replaceNull(null));
  }
  
  public synchronized List<Process> getProcesses(final String name, final String version, final String processName, final String profile) {
    return getProcesses(argFor(name), argFor(version), profile, argFor(processName));
  }
  
  public synchronized List<Process> getProcesses(Arg name, Arg version, String profile) {
    return getProcesses(name, version, profile, replaceNull(null));
  }
   
  public synchronized List<Process> getProcesses(final Arg name, final Arg version, final String profile,
                                 final Arg processName) {
    Matcher<Process> matcher = new CompositeMatcher<Process>()
    .add(
      new Matcher<Process>() {
        public boolean matches(Process object) {
          return name.matches(object.getDistributionInfo().getName());
        }
      }
    )
    .add(
      new Matcher<Process>() {
        public boolean matches(Process object) {
          return version.matches(object.getDistributionInfo().getVersion());
        }
      }
    )
    .add(
      new Matcher<Process>() {
        public boolean matches(Process object) {
          return processName.matches(object.getDistributionInfo().getProcessName());
        }
      }
    )
    .add(
      new Matcher<Process>() {
        public boolean matches(Process object) {
          if(profile == null) return true;
          return profile.equals(object.getDistributionInfo().getProfile());
        }
      }
    );
    
    return new IteratorFilter<Process>(matcher).filter(_processes.values()).sort(new ProcessComparator()).get();
  }

  public synchronized void removeProcess(String corusPid) {
    _processes.remove(corusPid);
  }

  public synchronized Process getProcess(String corusPid) throws ProcessNotFoundException {
    Process current = (Process) _processes.get(corusPid);

    if (current == null) {
      throw new ProcessNotFoundException("No process for ID: " + corusPid);
    }

    return current;
  }
  
  private Arg replaceNull(Arg someArg){
    if(someArg == null){
      return argFor(null);
    }
    else{
      return someArg;
    }
  }
  
  private Arg argFor(String arg){
    if(arg == null){
      return ArgFactory.any();
    }
    else{
      return ArgFactory.parse(arg);
    }
  }
}
