package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.event.EventSupport;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;

/**
 * This event occurs when a process is killed, either by the Corus server, the administrator,
 * or on request of the Corus process itself.
 * 
 * @author yduchesne
 *
 */
public class ProcessKilledEvent extends EventSupport {

  private ProcessTerminationRequestor requestor;
  private Process process;
  private boolean restarted;

  public ProcessKilledEvent(ProcessTerminationRequestor requestor, Process process, boolean restarted) {
    this.requestor = requestor;
    this.process = process;
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
      .field("restarted").value(restarted)
      .endObject();
  }

  @Override
  public EventLog toEventLog() {
    if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER) {
      if (!restarted) {
        return new EventLog(
            getTime(), 
            getLevel(), 
            "Processor", 
            getClass(), 
            "Process " + process.toString() + " was killed by Corus and was not restarted"
        );
      } else {
        return new EventLog(
            getTime(), 
            getLevel(), 
            "Processor", 
            getClass(),
            "Process " + process.toString() + " was killed by Corus and was restarted"
        );
      }
    } else if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN) {
      if (!restarted) {
        return new EventLog(
            getTime(), 
            getLevel(), 
            "Processor",
            getClass(),
            "Process " + process.toString() + " was killed by the administrator and was not restarted"
        );
      } else {
        return new EventLog(
            getTime(), 
            getLevel(), 
            "Processor", 
            getClass(),
            "Process " + process.toString() + " was killed by the administrator and was restarted"
        );
      }
    } else {
      if (!restarted) {
        return new EventLog(
            getTime(), 
            getLevel(), 
            "Processor", 
            getClass(), 
            "Process " + process.toString() + " requested termination"
        );
      } else {
        return new EventLog(
            getTime(), 
            EventLevel.INFO, 
            "Processor",
            getClass(),
            "Process " + process.toString() + " requested restart"
        );
      }
    }
  }

}
