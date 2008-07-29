package org.sapia.corus.processor.task;

import org.sapia.corus.LogicException;
import org.sapia.corus.port.PortManager;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.processor.task.action.ActionFactory;
import org.sapia.taskman.TaskContext;
import org.sapia.ubik.net.TCPAddress;


/**
 * This task suspends an active process.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SuspendTask extends ProcessTerminationTask {
  
  public SuspendTask(TCPAddress dynSvrAddress, int httpPort, String requestor, String corusPid,
                     ProcessDB db, int maxRetry, PortManager ports) {
    super(dynSvrAddress, httpPort, requestor, corusPid, db, maxRetry, ports);
  }
  
  /**
   * @see ProcessTerminationTask#onExec(TaskContext)
   */
  protected void onExec(TaskContext ctx) {
    try {
      Process process = db().getActiveProcesses().getProcess(corusPid());
      
      if(ActionFactory.newAttemptKillAction(requestor(), db(), process, getRetryCount()).execute(ctx)){
        abort();
      }
    } catch (LogicException e) {
      // no Vm for ID...
      super.abort();
      ctx.getTaskOutput().error(e);
    }
  }  

  /**
   * @see org.sapia.corus.processor.task.ProcessTerminationTask#onKillConfirmed(org.sapia.taskman.TaskContext)
   */
  protected void onKillConfirmed(TaskContext ctx) {
    try {
      Process process = db().getActiveProcesses().getProcess(corusPid());
      
      process.releasePorts(getPorts());

      synchronized (db()) {
        process.setStatus(Process.SUSPENDED);        
        db().getSuspendedProcesses().addProcess(process);
        db().getActiveProcesses().removeProcess(process.getProcessID());
      }

      ctx.getTaskOutput().warning("Process '" + process.getProcessID() +
                    "' put in suspended process queue.");
    } catch (LogicException e) {
      ctx.getTaskOutput().error(e);
    } finally {
      super.abort();
    }
  }
  
  /**
   * @see org.sapia.corus.processor.task.ProcessTerminationTask#onMaxRetry(org.sapia.taskman.TaskContext)
   */
  protected boolean onMaxRetry(TaskContext ctx) {
    if (ActionFactory.newForcefulKillAction(dynAddress(), httpPort(), requestor(), db(), corusPid(), -1, getPorts()).execute(ctx)) {
      onKillConfirmed(ctx);
      return true;
    } else {
      return false;
    }
  }
}
