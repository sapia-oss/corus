package org.sapia.corus.taskmanager;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskLog;

/**
 * Redirects Ant logging information to a wrapped {@link TaskLog}.
 * 
 * @author Yanick Duchesne
 */
public class AntTaskLog implements BuildListener {
  
  private TaskExecutionContext _context;;
  
  AntTaskLog(TaskExecutionContext context){
    _context = context;
  }

  /**
   * @see org.apache.tools.ant.BuildListener#messageLogged(BuildEvent)
   */
  public void messageLogged(BuildEvent evt) {
    String msg      = evt.getMessage();
    int    priority = evt.getPriority();

    switch (priority) {
      case Project.MSG_DEBUG:
        _context.debug(msg);
  
        break;
        
      case Project.MSG_INFO:
        _context.info(msg);

        break;

      case Project.MSG_WARN:
        _context.warn(msg);

        break;

      case Project.MSG_ERR:
        _context.error(msg);

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
