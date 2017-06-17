package org.sapia.corus.client.services.processor.event;

import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.event.EventLog.Level;
import org.sapia.corus.client.services.event.Loggable;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.util.Assertions;

public class ProcessAssumedKilledEvent implements Event, Loggable {

  private ProcessTerminationRequestor requestor;
  private Process process;
  private boolean restarted;

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

  public EventLog getEventLog() {
    if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER) {
      if (!restarted) {
        return new EventLog(Level.CRITICAL, "Processor", "Process " + ToStringUtil.toString(process) 
            + " was killed by Corus but an error occurred upon terminating it." 
            + " The process was not restarted. Check the status of the system to make sure that the process is indeed terminated");
      } else {
        return new EventLog(Level.CRITICAL, "Processor", "Process " + ToStringUtil.toString(process) 
            + " was killed by Corus but an error occurred upon terminating it." 
            + " The process was restarted. Check the status of the system to make sure that the original process was indeed terminated");
      }
    } else if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN) {
      if (!restarted) {
        return new EventLog(Level.CRITICAL, "Processor", "Process " + ToStringUtil.toString(process) 
            + " was killed by the administrator but an error occurred upon terminating it." 
            + " The processs was not restarted. Check the status of the system to make sure that the process is indeed terminated");
      } else {
        return new EventLog(Level.CRITICAL, "Processor", "Process " + ToStringUtil.toString(process) 
            + " was killed by the administrator but an error occurred upon terminating it." 
            + " The process was restarted. Check the status of the system to make sure that the original process was indeed terminated");
      }
    } else {
      if (!restarted) {
        return new EventLog(Level.CRITICAL, "Processor", "Process " + ToStringUtil.toString(process) 
            + " requested termination, but an error occurred upon trying to kill it." 
            + " Check the status of the system to make sure that the original process was indeed terminated");
      } else {
        return new EventLog(Level.CRITICAL, "Processor", "Process " + ToStringUtil.toString(process) 
            + " requested restart, but an error occurred upon trying to kill it."
            + " Check the status of the system to make sure that the original process was indeed terminated");
      }
    }
    
  }

}
