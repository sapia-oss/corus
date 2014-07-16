package org.sapia.corus.port.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.taskmanager.util.RunnableTask;

/**
 * Releases ports that are marked as busy but for which no running process
 * exists.
 * 
 * @author yduchesne
 * 
 */
public class PortCleanupTask extends RunnableTask {

  private static final int PRIME = 31;

  @Override
  public void run() {
    PortManager pm = context().getServerContext().getServices().getPortManager();
    Processor pc = context().getServerContext().getServices().getProcessor();
    List<Process> processes = pc.getProcessesWithPorts();
    List<PortRange> ranges = pm.getPortRanges();
    Set<PortKey> processPorts = new HashSet<PortKey>();
    for (Process p : processes) {
      for (ActivePort ap : p.getActivePorts()) {
        processPorts.add(new PortKey(ap.getName(), ap.getPort()));
      }
    }
    List<PortRange> toCleanup = new ArrayList<PortRange>(ranges);
    for (PortRange pr : toCleanup) {
      for (Integer ap : pr.getActive()) {
        if (!processPorts.contains(new PortKey(pr.getName(), ap))) {
          pm.releasePort(pr.getName(), ap);
        }
      }
    }
  }

  // ==========================================================================

  private static class PortKey {

    private String name;
    private int port;

    private PortKey(String name, int port) {
      this.name = name;
      this.port = port;
    }

    @Override
    public int hashCode() {
      return name.hashCode() * PRIME + port * PRIME;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof PortKey) {
        PortKey pk = (PortKey) obj;
        return pk.name.equals(name) && pk.port == port;
      }
      return false;
    }
  }

}
