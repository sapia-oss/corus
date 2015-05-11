package org.sapia.corus.processor;

import java.util.List;

import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.database.DbMap;
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

  private DbMap<String, Process> processes;

  public ProcessDatabaseImpl(DbMap<String, Process> map) {
    processes = map;
  }

  @Override
  public synchronized void addProcess(Process process) {
    processes.put(process.getProcessID(), process);
  }

  @Override
  public synchronized boolean containsProcess(String corusPid) {
    return processes.get(corusPid) != null;
  }

  @Override
  public synchronized void removeProcesses(ProcessCriteria criteria) {
    List<Process> toRemove = getProcesses(criteria);
    for (int i = 0; i < toRemove.size(); i++) {
      processes.remove(((Process) toRemove.get(i)).getProcessID());
    }
  }

  @Override
  public synchronized List<Process> getProcesses(final ProcessCriteria criteria) {
    Matcher<Process> matcher = new CompositeMatcher<Process>().add(new Matcher<Process>() {
      public boolean matches(Process p) {
        return p.matches(criteria);
      }
    });
    return new IteratorFilter<Process>(matcher).filter(processes.values()).sort(new ProcessComparator()).get();
  }

  public synchronized void removeProcess(String corusPid) {
    processes.remove(corusPid);
  }

  public synchronized Process getProcess(String corusPid) throws ProcessNotFoundException {
    Process current = (Process) processes.get(corusPid);

    if (current == null) {
      throw new ProcessNotFoundException("No process for ID: " + corusPid);
    }

    return current;
  }
  
  @Override
  public synchronized void dump(JsonStream stream) {
    processes.dump(stream);
  }
  
  @Override
  public synchronized void load(JsonInput dump) {
    processes.load(dump);
  }
  
  @Override
  public String toString() {
    return Strings.toString("processes", processes);
  }
}
