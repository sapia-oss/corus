package org.sapia.corus.processor.task.action;

import java.io.File;

import org.sapia.corus.processor.ProcessInfo;
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
public class MakeProcessDirAction implements Action{
  
  private ProcessInfo _info;
  private File        _processDir;
  
  public MakeProcessDirAction(ProcessInfo info){
    _info = info;
  } 
  
  /**
   * @see org.sapia.corus.taskmanager.Action#execute(org.sapia.taskman.TaskContext)
   */
  public boolean execute(TaskContext ctx) {
    File processDir = new File(_info.getDistribution().getProcessesDir() +
                               File.separator +
                               _info.getProcess().getProcessID());

    if (_info.isRestart() && !processDir.exists()) {
      ctx.getTaskOutput().warning("Process directory: " + processDir +
                    " does not exist; restart aborted");
      return false;
    } else {
      processDir.mkdirs();

      if (!processDir.exists()) {
        ctx.getTaskOutput().warning("Could not make process directory: " + processDir +
                      "; startup aborted");

        return false;
      }
    }
    
    _processDir = processDir;
    return true;
  }
  
  public File getProcessDir(){
    return _processDir;
  }

}
