package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLogCapable;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.rmi.interceptor.Event;

public class ProcessStaleEvent implements Event, EventLogCapable {

  private Process process;

  public ProcessStaleEvent(Process process) {
    this.process = process;
  }

  public Process getProcess() {
    return process;
  }

  public EventLog toEventLog() {
    return new EventLog(EventLevel.CRITICAL, "Processor", "Process " + process.toString() + " has been detected as stale");
  }

}
