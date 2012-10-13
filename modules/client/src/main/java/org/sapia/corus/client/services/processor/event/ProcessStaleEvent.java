package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.event.EventLog.Level;
import org.sapia.corus.client.services.event.Loggable;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.rmi.interceptor.Event;

public class ProcessStaleEvent implements Event, Loggable{

  private Process 									  process;
  
  public ProcessStaleEvent(Process process) {
    this.process = process;
  }
  
  public Process getProcess() {
    return process;
  }
  
  public EventLog getEventLog() {
    return new EventLog(Level.CRITICAL, "Processor", "Process " + process.toString() +  " has been detected as stale");
  }

}
