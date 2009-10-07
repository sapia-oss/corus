package org.sapia.corus.taskmanager;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskLog;

/**
 * Redirects Ant logging information to a wrapped {@link TaskLog}.
 * 
 * @author Yanick Duchesne
 */
public class AntTaskLog implements BuildListener {
  
  private Task _task;
  private TaskLog _out;
  
  AntTaskLog(Task task, TaskLog out){
    _task = task;
    _out = out;
  }

  /**
   * @see org.apache.tools.ant.BuildListener#messageLogged(BuildEvent)
   */
  public void messageLogged(BuildEvent evt) {
    String msg      = evt.getMessage();
    int    priority = evt.getPriority();

    switch (priority) {
      case Project.MSG_DEBUG:
        _out.debug(_task, msg);
  
        break;
        
      case Project.MSG_INFO:
        _out.info(_task, msg);

        break;

      case Project.MSG_WARN:
        _out.warn(_task, msg);

        break;

      case Project.MSG_ERR:
        _out.error(_task, msg);

        break;
    }
  }

  public void buildFinished(BuildEvent arg0) {
  }

  public void buildStarted(BuildEvent arg0) {
  }

  public void targetFinished(BuildEvent arg0) {
  }

  public void targetStarted(BuildEvent arg0) {
  }

  public void taskFinished(BuildEvent arg0) {
  }

  public void taskStarted(BuildEvent arg0) {
  }
}
