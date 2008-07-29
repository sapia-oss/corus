package org.sapia.corus.processor.task.action;

import java.io.File;

import org.sapia.console.CmdLine;
import org.sapia.corus.processor.Process;
import org.sapia.taskman.TaskContext;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TestExecCmdLineAction extends ExecCmdLineAction{
  
  TestExecCmdLineAction(File procDir, CmdLine cmd, Process proc){
    super(procDir, cmd, proc);
  }
  /**
   * @see org.sapia.corus.taskmanager.Action#execute(org.sapia.taskman.TaskContext)
   */
  public boolean execute(TaskContext ctx) {
    return true;
  }

}
