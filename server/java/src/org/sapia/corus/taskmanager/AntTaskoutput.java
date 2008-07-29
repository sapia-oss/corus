package org.sapia.corus.taskmanager;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.sapia.taskman.TaskOutput;

/**
 * Redirects Ant logging information to a wrapped <code>TaskOutput</code>.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class AntTaskoutput implements BuildListener {
  
  private TaskOutput _out;
  
  AntTaskoutput(TaskOutput out){
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
        _out.debug(msg);
  
        break;
        
      case Project.MSG_INFO:
        _out.info(msg);

        break;

      case Project.MSG_WARN:
        _out.warning(msg);

        break;

      case Project.MSG_ERR:
        _out.error(msg);

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
