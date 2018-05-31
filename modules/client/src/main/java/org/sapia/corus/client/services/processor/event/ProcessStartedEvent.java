package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.event.EventLog.Level;
import org.sapia.corus.client.services.event.Loggable;
import org.sapia.corus.client.services.processor.Process;

/**
 * Signals that the Corus server has started a process.
 * 
 * @author yduchesne
 * 
 */
public class ProcessStartedEvent implements Loggable {

  private Distribution  distribution;
  private ProcessConfig processConfig;
  private Process       process; 
  
  public ProcessStartedEvent(Distribution dist, ProcessConfig processConfig, Process process) {
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
  
  @Override
  public EventLog getEventLog() {
    return new EventLog(Level.NORMAL, "Processor", "Process was started: " + processConfig.toString() + ", id = " + process.getProcessID());
  }

}
