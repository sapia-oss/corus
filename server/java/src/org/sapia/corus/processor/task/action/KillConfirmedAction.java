package org.sapia.corus.processor.task.action;

import org.sapia.corus.port.PortManager;
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
public class KillConfirmedAction implements Action{
  
  private ProcessDB _db;
  private Process   _proc;
  private PortManager _ports;
  
  public KillConfirmedAction(ProcessDB db, Process proc, PortManager ports){
    _db = db;
    _proc = proc;
    _ports = ports;
  }
  
  /**
   * @see org.sapia.corus.taskmanager.Action#execute(org.sapia.taskman.TaskContext)
   */
  public boolean execute(TaskContext ctx) {
    ctx.getTaskOutput().info("Process kill confirmed: " + _proc.getProcessID());
    _proc.releasePorts(_ports);
    if(!new CleanupProcessAction(_db, _proc).execute(ctx)){
      return false;
    }
    ctx.getTaskOutput().warning("Process " + _proc.getProcessID() + " terminated");
    return true;
  }
}
