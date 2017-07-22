package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.event.CorusEventSupport;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Processor;

/**
 * Dispatched when a process' unpublishing is pending.
 * 
 * @author yduchesne
 *
 */
public class ProcessUnpublishingPendingEvent extends CorusEventSupport {
  
  private Process process;
  
  public ProcessUnpublishingPendingEvent(Process process) {
    this.process = process;
  }
  
  public Process getProcess() {
    return process;
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
        .message("Unpublishing of process %s is pending", ToStringUtil.toString(process))
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
      .field("process");
    process.toJson(stream, ContentLevel.SUMMARY);
  }

}
