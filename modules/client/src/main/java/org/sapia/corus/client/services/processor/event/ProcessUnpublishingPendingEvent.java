package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.services.processor.Process;

/**
 * Dispatched when a process' unpublishing is pending.
 * 
 * @author yduchesne
 *
 */
public class ProcessUnpublishingPendingEvent {
  
  private Process process;
  
  public ProcessUnpublishingPendingEvent(Process process) {
    this.process = process;
  }
  
  public Process getProcess() {
    return process;
  }

}
