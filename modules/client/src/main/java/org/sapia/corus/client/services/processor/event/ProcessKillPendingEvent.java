package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Dispatched when a process kill has been requested and is pending.
 * 
 * @author yduchesne
 *
 */
public class ProcessKillPendingEvent implements Event {

  private ProcessTerminationRequestor requestor;
  private Process process;

  public ProcessKillPendingEvent(ProcessTerminationRequestor requestor, Process process) {
    this.requestor = requestor;
    this.process = process;
  }

  public Process getProcess() {
    return process;
  }

  public ProcessTerminationRequestor getRequestor() {
    return requestor;
  }

}
