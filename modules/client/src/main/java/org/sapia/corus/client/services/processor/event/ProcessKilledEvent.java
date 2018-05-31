package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.event.EventLog.Level;
import org.sapia.corus.client.services.event.Loggable;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;

public class ProcessKilledEvent implements Loggable {

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

  public EventLog getEventLog() {
    if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER) {
      if (!restarted) {
        return new EventLog(Level.CRITICAL, "Processor", "Process " +ToStringUtil.toString(process)
          + " was killed by Corus and was not restarted");
      } else {
        return new EventLog(Level.NORMAL, "Processor", "Process " + ToStringUtil.toString(process)
          + " was killed by Corus and was restarted");
      }
    } else if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN) {
      if (!restarted) {
        return new EventLog(Level.NORMAL, "Processor", "Process " + ToStringUtil.toString(process)
          + " was killed by the administrator and was not restarted");
      } else {
        return new EventLog(Level.NORMAL, "Processor", "Process " + ToStringUtil.toString(process)
          + " was killed by the administrator and was restarted");
      }
    } else {
      if (!restarted) {
        return new EventLog(Level.NORMAL, "Processor", "Process " + ToStringUtil.toString(process)
          + " requested termination");
      } else {
        return new EventLog(Level.NORMAL, "Processor", "Process " + ToStringUtil.toString(process)
          + " requested restart");
      }
    }
  }

}
