package org.sapia.corus.processor.task;

import org.sapia.corus.LogicException;
import org.sapia.corus.port.PortManager;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.processor.task.action.ActionFactory;
import org.sapia.taskman.TaskContext;
import org.sapia.ubik.net.TCPAddress;


/**
 * This task insures the destruction of a given process.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class KillTask extends ProcessTerminationTask {
  
  private long    _restartInterval;

  /**
   * Constructs an instance of this class with the given params.
   *
   * @param db a <code>ProcessDB</code>
   * @param corusPid a process identifier.
   * @param maxRetry the maximum number of times this instance must
   * try to kill a given process before it proceeds to an "OS" kill.
   */
  public KillTask(TCPAddress dynSvrAddress, int httpPort, String requestor, String corusPid,
           ProcessDB db, int maxRetry, long restartIntervalMillis, PortManager ports) {
    super(dynSvrAddress, httpPort, requestor, corusPid, db, maxRetry, ports);
    _restartInterval = restartIntervalMillis;
  }

  /**
   * @see org.sapia.corus.processor.task.ProcessTerminationTask#onKillConfirmed(org.sapia.taskman.TaskContext)
   */
  protected void onKillConfirmed(TaskContext ctx) {
    try {
      
      Process process = db().getActiveProcesses().getProcess(corusPid());
      ctx.getTaskOutput().info("Process kill confirmed: " + process.getProcessID());
      if(!ActionFactory.newCleanupProcessAction(db(), process).execute(ctx)){
        ctx.getTaskOutput().warning("Process " + corusPid() + " will not be retarted");
        return;
      }
      if (requestor().equals(Process.KILL_REQUESTOR_SERVER)) {
        if ((System.currentTimeMillis() - process.getCreationTime()) > _restartInterval) {
          ctx.getTaskOutput().warning("Restarting process: " + process);
          ActionFactory.newRestartVmAction(dynAddress(), httpPort(), db(), process, getPorts()).execute(ctx);
          onRestarted();
        } else {
          ctx.getTaskOutput().warning("Process will not be restarted; not enough time since last restart");
        }
      } else {
        ctx.getTaskOutput().warning("Process " + corusPid() + " terminated");
      }
    } catch (LogicException e) {
      ctx.getTaskOutput().error(e.getMessage());
    } finally {
      super.abort();
    }
  }
  
  /**
   * @see org.sapia.corus.processor.task.ProcessTerminationTask#onMaxRetry(org.sapia.taskman.TaskContext)
   */
  protected boolean onMaxRetry(TaskContext ctx) {
    return ActionFactory.newForcefulKillAction(dynAddress(), httpPort(), requestor(), db(), corusPid(), _restartInterval, getPorts()).execute(ctx);
  }

  /**
   * @see org.sapia.corus.processor.task.ProcessTerminationTask#onExec(org.sapia.taskman.TaskContext)
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
  
  protected void onRestarted(){}
}
