package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.event.CorusEventSupport;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.processor.ProcessStartupInfo;
import org.sapia.corus.client.services.processor.Processor;

/**
 * Dispatched when process execution is pending.
 * 
 * @author yduchesne
 *
 */
public class ProcessStartPendingEvent extends CorusEventSupport {
  
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

  public ProcessConfig getProcessConfig() {
    return process;
  }
  
  public ProcessStartupInfo getStartupInfo() {
    return startupInfo;
  }
  
  @Override
  public EventLevel getLevel() {
    return EventLevel.INFO;
  }
  
  @Override
  public EventLog toEventLog() {
    return EventLog.builder()
        .source(source())
        .type(getClass())
        .level(getLevel())
        .message(
            "Process execution pending for process %s of %s - %s instance(s) requested", 
            process.getName(), ToStringUtil.toString(distribution), startupInfo.getRequestedInstances())
        .build();
  }

  // --------------------------------------------------------------------------
  // Restricted
  
  @Override
  protected Class<?> source() {
    return Processor.class;
  }
  
  @Override
  protected void toJson(JsonStream stream) {
    stream
      .field("message").value(toEventLog().getMessage())
      .field("processName").value(process.getName())
      .field("requestedInstances").value(startupInfo.getRequestedInstances())
      .field("distribution");
    distribution.toJson(stream, ContentLevel.SUMMARY);
  }
  
}
