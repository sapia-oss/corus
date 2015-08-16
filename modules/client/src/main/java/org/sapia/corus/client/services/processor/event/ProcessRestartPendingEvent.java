package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Signals that a process restart has been requested and is pending.
 * 
 * @author yduchesne
 * 
 */
public class ProcessRestartPendingEvent implements Event {

  private Distribution  distribution;
  private ProcessConfig processConfig;
  private Process       process; 
  
  public ProcessRestartPendingEvent(Distribution dist, ProcessConfig processConfig, Process process) {
    this.distribution  = dist;
    this.processConfig = processConfig;
    this.process       = process;
  }

  public Distribution getDistribution() {
    return distribution;
  }

  public ProcessConfig getProcessConfig() {
    return processConfig;
  }
  
  public Process getProcess() {
    return process;
  }

}
