package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.event.CorusEventSupport;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.client.services.processor.Processor;

/**
 * This event occurs when a process is killed, either by the Corus server, the administrator,
 * or on request of the Corus process itself.
 * 
 * @author yduchesne
 *
 */
public class ProcessKilledEvent extends CorusEventSupport {

  private ProcessTerminationRequestor requestor;
  private Process process;
  private boolean restarted;

  public ProcessKilledEvent(ProcessTerminationRequestor requestor, Process process, boolean restarted) {
    this.requestor = requestor;
    this.process   = process;
    this.restarted = restarted;
  }

  public Process getProcess() {
    return process;
  }

  public ProcessTerminationRequestor getRequestor() {
    return requestor;
  }

  public boolean wasRestarted() {
    return restarted;
  }
  
  @Override
  public EventLevel getLevel() {
    return requestor == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER ? EventLevel.CRITICAL : EventLevel.INFO;
  }

  @Override
  public EventLog toEventLog() {
    if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER) {
      if (!restarted) {
        return EventLog.builder()
            .source(source())
            .type(getClass())
            .level(getLevel())
            .message("Process %s was killed by Corus and was not restarted", ToStringUtil.toString(process))
            .build();
      } else {
        return EventLog.builder()
            .source(source())
            .type(getClass())
            .level(getLevel())
            .message("Process %s was killed by Corus and was restarted", ToStringUtil.toString(process))
            .build();
      }
    } else if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN) {
      if (!restarted) {
        return EventLog.builder()
            .source(source())
            .type(getClass())
            .level(getLevel())
            .message("Process %s was killed by the administrator", ToStringUtil.toString(process))
            .build();
      } else {
        return EventLog.builder()
            .source(source())
            .type(getClass())
            .level(getLevel())
            .message("Process %s was killed by Corus and restarted by the administrator", ToStringUtil.toString(process))
            .build();
      }
    } else {
      if (!restarted) {
        return EventLog.builder()
            .source(source())
            .type(getClass())
            .level(getLevel())
            .message("Process %s requested termination and was terminated", ToStringUtil.toString(process))
            .build();
      } else {
        return EventLog.builder()
            .source(source())
            .type(getClass())
            .level(getLevel())
            .message("Process %s requested a restart and was restarted", ToStringUtil.toString(process))
            .build();
      }
    }
  }
  
  @Override
  protected Class<?> source() {
    return Processor.class;
  }
  
  @Override
  protected void toJson(JsonStream stream) {
    stream
      .field("requestor").value(requestor.name())
      .field("restarted").value(restarted)
      .field("message").value(toEventLog().getMessage())
      .field("process");
    process.toJson(stream, ContentLevel.DETAIL);
  }
}
