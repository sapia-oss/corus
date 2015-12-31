package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.event.EventLog.Level;
import org.sapia.corus.client.services.event.Loggable;
import org.sapia.corus.client.services.processor.ProcessStartupInfo;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Dispatched when process execution is pending.
 * 
 * @author yduchesne
 *
 */
public class ProcessStartPendingEvent implements Event, Loggable {
  
  private Distribution       distribution;
  private ProcessConfig      process;
  private ProcessStartupInfo startupInfo;

  public ProcessStartPendingEvent(Distribution dist, ProcessConfig process, ProcessStartupInfo info) {
    this.distribution = dist;
    this.process      = process;
    this.startupInfo  = info;
  }

  public Distribution getDistribution() {
    return distribution;
  }

  public ProcessConfig getProcess() {
    return process;
  }
  
  public ProcessStartupInfo getStartupInfo() {
    return startupInfo;
  }
  
  @Override
  public EventLog getEventLog() {
    return new EventLog(Level.NORMAL, "Processor", 
        String.format(
            "Process execution pending for %s - %s instance(s) requested", 
            process, startupInfo.getRequestedInstances()
        )
    );
  }

}
