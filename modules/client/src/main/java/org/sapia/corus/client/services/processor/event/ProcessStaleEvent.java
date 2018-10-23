package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.event.CorusEventSupport;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Processor;

public class ProcessStaleEvent extends CorusEventSupport {

  private Process process;

  public ProcessStaleEvent(Process process) {
    this.process = process;
  }

  public Process getProcess() {
    return process;
  }
  
  @Override
  public EventLevel getLevel() {
    return EventLevel.ERROR;
  }
  
  public EventLog toEventLog() {
    return EventLog.builder()
        .source(source())
        .type(getClass())
        .level(getLevel())
        .message("Process %s has been detected as stale", ToStringUtil.toString(process))
        .build();
  }

  // --------------------------------------------------------------------------
  // Restricted
  
  @Override
  protected void toJson(JsonStream stream) {
    stream
      .field("message").value(toEventLog().getMessage())
      .field("process");
    process.toJson(stream, ContentLevel.SUMMARY);
  }
  
  @Override
  protected Class<?> source() {
    return Processor.class;
  }
}
