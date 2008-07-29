package org.sapia.corus.processor.task.action;

import java.io.File;

import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.taskmanager.Action;
import org.sapia.corus.taskmanager.tasks.TaskFactory;
import org.sapia.taskman.TaskContext;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CleanupProcessAction implements Action{
  
  private ProcessDB _db;
  private Process   _proc;
  
  public CleanupProcessAction(ProcessDB db, Process proc){
    _db = db;
    _proc = proc;
  }
  
  /**
   * @see org.sapia.corus.taskmanager.Action#execute(org.sapia.taskman.TaskContext)
   */
  public boolean execute(TaskContext ctx) {
    if (_proc.getProcessDir() != null) {
      if(_proc.isDeleteOnKill()){
        File f = new File(_proc.getProcessDir());
        //ctx.execSyncNestedTask( "DeleteFileTask", TaskFactory.newDeleteDirTask(f));
        TaskFactory.newDeleteDirTask(f).exec(ctx);

        if (f.exists()) {
          ctx.getTaskOutput().warning("Could not destroy process directory: " +
                        f.getAbsolutePath());
        }
      }
    }

    _db.getActiveProcesses().removeProcess(_proc.getProcessID());
    if(_proc.isDeleteOnKill()){
      ctx.getTaskOutput().warning("Process successfully terminated and cleaned up: " + _proc.getProcessID());
    }
    else{
      ctx.getTaskOutput().warning("Process successfully terminated: " + _proc.getProcessID());
    }
    return true;
  }

}
