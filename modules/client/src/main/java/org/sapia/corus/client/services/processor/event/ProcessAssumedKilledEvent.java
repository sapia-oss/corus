package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.event.CorusEventSupport;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.client.services.processor.Processor;

public class ProcessAssumedKilledEvent extends CorusEventSupport {

  private ProcessTerminationRequestor requestor;
  private Process                     process;
  private boolean                     restarted;

  public ProcessAssumedKilledEvent(ProcessTerminationRequestor requestor, Process process, boolean restarted) {
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
    return EventLevel.WARNING;
  }

  @Override
  public EventLog toEventLog() {
    if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER) {
      if (!restarted) {
        return EventLog.builder()
          .source(source())
          .type(ProcessAssumedKilledEvent.class)
          .level(getLevel())
          .message("Process %s was killed by Corus ", ToStringUtil.toString(process))
          .message("but an error occurred upon terminating it. ")
          .message("The process was not restarted. Check the status of the system ")
          .message("to make sure that the process is indeed terminated")
          .build();
      } else {
        return EventLog.builder()
          .source(source())
          .type(ProcessAssumedKilledEvent.class)
          .level(getLevel())
          .message("Process %s was killed by Corus ", ToStringUtil.toString(process))
          .message("but an error occurred upon terminating it. ")
          .message("The process was restarted. Check the status of the system ")
          .message("to make sure that the original process was indeed terminated")
          .build();
      }
    } else if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN) {
      if (!restarted) {
        return EventLog.builder()
          .source(source())
          .type(ProcessAssumedKilledEvent.class)
          .level(getLevel())
          .message("Process %s was killed by the administrator ", ToStringUtil.toString(process))
          .message("but an error occurred upon terminating it. ")
          .message("The process was not restarted. Check the status of the system ")
          .message("to make sure that the process is indeed terminated")
          .build();
      } else {
        return EventLog.builder()
          .source(source())
          .type(ProcessAssumedKilledEvent.class)
          .level(getLevel())
          .message("Process %s was killed by the administrator ", ToStringUtil.toString(process))
          .message("but an error occurred upon terminating it. ")
          .message("The process was restarted. Check the status of the system ")
          .message("to make sure that the original process was indeed terminated")
          .build();
      }
    } else {
      if (!restarted) {
        return EventLog.builder()
          .source(source())
          .type(ProcessAssumedKilledEvent.class)
          .level(getLevel())
          .message("Process %s has requested termination ", ToStringUtil.toString(process))
          .message("but an error occurred upon trying to kill it. ")
          .message("Check the status of the system ")
          .message("to make sure that the process is indeed terminated")
          .build();
      } else {
        return EventLog.builder()
          .source(source())
          .type(ProcessAssumedKilledEvent.class)
          .level(getLevel())
          .message("Process %s has requested a restart ", ToStringUtil.toString(process))
          .message("but an error occurred upon trying to kill it. ")
          .message("The process was restarted regardless, but check the status of the system ")
          .message("to make sure that the original process was indeed terminated")
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
    
    process.toJson(stream, ContentLevel.SUMMARY);
  }

}
