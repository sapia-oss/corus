package org.sapia.corus.taskmanager;

import org.sapia.taskman.TaskContext;

/**
 * An action is called in the context of a task.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface Action {
  
  /**
   * Performs an atomic unit of work and returns <code>true</code> if the
   * outcome is successful.
   * 
   * @param ctx
   * @return the result - if <code>true</code>, the action was successful.
   */
  public boolean execute(TaskContext ctx);
}
