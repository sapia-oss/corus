package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.event.EventSupport;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;

/**
 * Dispatched when a process kill has been requested and is pending.
 * 
 * @author yduchesne
 *
 */
public class ProcessKillPendingEvent extends EventSupport {

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
  
  @Override
  public EventLevel getLevel() {
    return requestor == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER ? EventLevel.CRITICAL : EventLevel.INFO;
  }
 
  // --------------------------------------------------------------------------
  // JsonStreamable interface
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("type").value(getClass().getSimpleName())
      .field("level").value(getLevel().name())
      .field("time").value(formattedTime())
      .field("requestor").value(requestor.name())
      .field("processId").value(process.getProcessID())
      .field("processName").value(process.getDistributionInfo().getProcessName())
      .field("distribution").value(process.getDistributionInfo().getName())
      .field("version").value(process.getDistributionInfo().getVersion())
      .endObject();
  }
  
  @Override
  public EventLog toEventLog() {
    switch (requestor) {
      case KILL_REQUESTOR_SERVER:
        return EventLog.builder()
            .time(getTime())
            .level(getLevel())
            .source("Processor")
            .type(getClass())
            .message("Automatic process termination started for process %s", ToStringUtil.toString(process))
            .build();
      case KILL_REQUESTOR_ADMIN:
        return EventLog.builder()
            .time(getTime())
            .level(getLevel())
            .source("Processor")
            .type(getClass())
            .message("Process termination requested by admin for process %s", ToStringUtil.toString(process))
            .build();
      case KILL_REQUESTOR_PROCESS:
        return EventLog.builder()
            .time(getTime())
            .level(getLevel())
            .source("Processor")
            .type(getClass())
            .message("Process %s requested its own termination", ToStringUtil.toString(process))
            .build();
      default:
        throw new IllegalStateException("Requestor type not handled: " + requestor);
      
    }
  }

}
