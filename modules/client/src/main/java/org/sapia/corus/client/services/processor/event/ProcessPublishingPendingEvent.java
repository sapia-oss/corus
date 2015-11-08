package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Dispatched when a process' publishing is pending.
 * 
 * @author yduchesne
 *
 */
public class ProcessPublishingPendingEvent implements Event {
  
  private Process process;
  
  public ProcessPublishingPendingEvent(Process process) {
    this.process = process;
  }
  
  public Process getProcess() {
    return process;
  }

}
