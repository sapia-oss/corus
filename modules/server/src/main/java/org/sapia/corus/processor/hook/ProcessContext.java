package org.sapia.corus.processor.hook;

import org.sapia.corus.client.services.processor.Process;

/**
 * Holds process-related information, corresponding to a process that should be killed or
 * started.
 * 
 * @author yduchesne
 *
 */
public class ProcessContext {
  
  private Process process;

  public ProcessContext(Process process) {
    this.process       = process;
  }
  
  public Process getProcess() {
    return process;
  }
}
