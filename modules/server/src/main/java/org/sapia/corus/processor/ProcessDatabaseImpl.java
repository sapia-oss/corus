package org.sapia.corus.processor;

import java.util.List;

import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.util.CompositeMatcher;
import org.sapia.corus.util.IteratorFilter;
import org.sapia.corus.util.Matcher;
import org.sapia.ubik.util.Strings;

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

  @Override
  public synchronized void addProcess(Process process) {
    _processes.put(process.getProcessID(), process);
  }

  @Override
  public synchronized boolean containsProcess(String corusPid) {
    return _processes.get(corusPid) != null;
  }

  @Override
  public synchronized void removeProcesses(ProcessCriteria criteria) {
    List<Process> toRemove = getProcesses(criteria);
    for (int i = 0; i < toRemove.size(); i++) {
      _processes.remove(((Process) toRemove.get(i)).getProcessID());
    }
  }

  @Override
  public synchronized List<Process> getProcesses(final ProcessCriteria criteria) {
    Matcher<Process> matcher = new CompositeMatcher<Process>().add(new Matcher<Process>() {
      public boolean matches(Process object) {
        return criteria.getDistribution().matches(object.getDistributionInfo().getName());
      }
    }).add(new Matcher<Process>() {
      public boolean matches(Process object) {
        return criteria.getVersion().matches(object.getDistributionInfo().getVersion());
      }
    }).add(new Matcher<Process>() {
      public boolean matches(Process object) {
        return criteria.getName().matches(object.getDistributionInfo().getProcessName());
      }
    }).add(new Matcher<Process>() {
      public boolean matches(Process object) {
        if (criteria.getProfile() == null)
          return true;
        return criteria.getProfile().equals(object.getDistributionInfo().getProfile());
      }
    }).add(new Matcher<Process>() {
      public boolean matches(Process object) {
        return criteria.getPid().matches(object.getProcessID());
      }
    }).add(new Matcher<Process>() {
      public boolean matches(Process object) {
        return true;
      }
    }).add(new Matcher<Process>() {
      @Override
      public boolean matches(Process object) {
        if (criteria.getLifeCycles().isEmpty()) {
          return true;
        }
        return criteria.getLifeCycles().contains(object.getStatus());
      }
      
    });

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
  
  @Override
  public String toString() {
    return Strings.toString("processes", _processes);
  }
}
