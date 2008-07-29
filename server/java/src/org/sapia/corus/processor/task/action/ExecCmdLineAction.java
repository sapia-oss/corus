package org.sapia.corus.processor.task.action;

import java.io.File;
import java.io.IOException;

import org.sapia.console.CmdLine;
import org.sapia.corus.processor.NativeProcess;
import org.sapia.corus.processor.NativeProcessFactory;
import org.sapia.corus.processor.Process;
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
public class ExecCmdLineAction implements Action{
  
  private CmdLine _cmd;
  private File    _procDir;
  private Process _proc;
  
  public ExecCmdLineAction(File procDir, CmdLine cmd, Process proc){
    _cmd  = cmd;
    _procDir = procDir;
    _proc = proc;
  }
  
  /**
   * @see org.sapia.corus.taskmanager.Action#execute(org.sapia.taskman.TaskContext)
   */
  public boolean execute(TaskContext ctx) {
    NativeProcess proc = NativeProcessFactory.newNativeProcess();

    try {
      _proc.setOsPid(proc.exec(ctx, _procDir, _cmd));
    } catch (IOException e) {
      ctx.getTaskOutput().info("Process could not be started");
      ctx.getTaskOutput().error(e);
      return false;
    }

    ctx.getTaskOutput().info("Process started; corus pid: " + _proc.getProcessID());

    if (_proc.getOsPid() == null) {
      ctx.getTaskOutput().info("No os pid available");
    } else {
      ctx.getTaskOutput().info("OS pid: " + _proc.getOsPid());
    }
    return true;
  }

}
