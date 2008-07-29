package org.sapia.corus.taskmanager;

import org.sapia.taskman.TaskManager;
import org.sapia.taskman.TaskOutput;

/**
 * Implements Corus's task manager.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CorusTaskman extends TaskManager{
  
  private TaskOutputLogger _logger;
  
  CorusTaskman(TaskOutputLogger logger){
    super("corus.taskManager");
    _logger = logger;
  }
  
  /**
   * @see org.sapia.taskman.TaskManager#newTaskOutput(java.lang.String)
   */
  protected TaskOutput newTaskOutput(String taskName) {
    _logger.setTaskName(taskName);
    return _logger;

  }
  
  

}
