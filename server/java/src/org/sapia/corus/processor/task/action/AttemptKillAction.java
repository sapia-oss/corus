package org.sapia.corus.processor.task.action;

import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.taskmanager.Action;
import org.sapia.taskman.TaskContext;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class AttemptKillAction implements Action{
  
  private String _requestor;
  private ProcessDB _db;
  private Process _proc;
  private int     _retryCount;
  
  public AttemptKillAction(String requestor, ProcessDB db, Process proc, int currentRetryCount){
    _requestor = requestor;
    _db = db;
    _proc = proc;
    _retryCount = currentRetryCount;
  }
  
  /**
   * @see org.sapia.corus.taskmanager.Action#execute(org.sapia.taskman.TaskContext)
   */
  public boolean execute(TaskContext ctx) {
    if (_proc.getStatus() == Process.KILL_CONFIRMED) {
      ctx.getTaskOutput().info("Process " + _proc.getProcessID() +
                        " has confirmed shutdown");
      return true;
    }
    ctx.getTaskOutput().info("Killing process " + _proc + ". Attempt: " + _retryCount + "; requestor: " + _requestor);
    _proc.kill(_requestor);
    return false;
  }

}
