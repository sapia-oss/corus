package org.sapia.corus.taskmanager;

import java.rmi.Remote;

import org.sapia.corus.Module;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.taskman.Task;
import org.sapia.taskman.TaskDescriptor;
import org.sapia.taskman.TaskOutput;


/**
 * Specifies a task management API. 
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface TaskManager extends Module, Remote{
  String ROLE = TaskManager.class.getName();

  /**
   * Executes the given task synchronously - in the caller's thread.
   * 
   * @param name the name of the task to execute.
   * @param task a <code>Task</code> instance.
   * @return a <code>ProgressQueue</code>.
   */
  public ProgressQueue execSyncTask(String name, Task task);

  /**
   * Executes the given task synchronously - in the caller's thread.
   * 
   * @param name the name of the task to execute.
   * @param task a <code>Task</code> instance.
   * @param out the <code>TaskOutput</code> to which the task should
   * log information.
   */
  public void execSyncTask(String name, Task task, TaskOutput out);
  
  /**
   * Executes the given task asynchronously - in the caller's thread.
   * 
   * @param name the name of the task to execute.
   * @param task a <code>Task</code> instance.
   * @param out the <code>TaskOutput</code> to which the task should
   * log information.
   */  
  public void execAsyncTask(String name, Task task, TaskOutput out);
  
  /**
   * Executes the given task asynchronously - in the caller's thread.
   * 
   * @param name the name of the task to execute.
   * @param task a <code>Task</code> instance.
   */  
  public void execAsyncTask(String name, Task task);  

  /**
   * Executes the task wrapped by the given descriptor.
   * 
   * @param td a <code>TaskDescriptor</code>
   */
  public void execTaskFor(TaskDescriptor td);  
  
	/**
	 * Returns a <code>ProgressQueue</code> that can be used to monitor ongoing
	 * tasks.
	 * 
	 * @param level a "debug level", as specified in the constants 
	 * of the <code>ProgressMsg</code> class.
	 * @return a <code>ProgressQueue</code>
	 * @see org.sapia.corus.util.ProgressMsg
	 */
	public ProgressQueue getProgressQueue(int level);  
}
