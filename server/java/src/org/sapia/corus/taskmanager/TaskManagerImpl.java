package org.sapia.corus.taskmanager;

import org.sapia.corus.ModuleHelper;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.corus.util.ProgressQueueImpl;
import org.sapia.taskman.Task;
import org.sapia.taskman.TaskDescriptor;
import org.sapia.taskman.TaskOutput;


/**
 * This module implements the <code>TaskManager</code> interface.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TaskManagerImpl extends ModuleHelper implements TaskManager{
  
  private CorusTaskman _taskMan; 
  private ProgressQueues _queues = new ProgressQueues();

  /**
   * @see org.sapia.soto.Service#init()
   */
  public void init() throws Exception {
    _taskMan = new CorusTaskman(new TaskOutputLogger(_queues, logger()));
    _taskMan.setDaemon(true);
    _taskMan.start();
  }

  /**
   * @see org.sapia.soto.Service#dispose()
   */
  public void dispose() {
    _taskMan.shutdown();
  }

  /*////////////////////////////////////////////////////////////////////
                         Module INTERFACE METHODS
  ////////////////////////////////////////////////////////////////////*/

  /**
   * @see org.sapia.corus.Module#getRoleName()
   */
  public String getRoleName() {
    return TaskManager.ROLE;
  }

  /*////////////////////////////////////////////////////////////////////
                       TaskManager INTERFACE METHODS
  ////////////////////////////////////////////////////////////////////*/
  
  /**
   * @see org.sapia.corus.taskmanager.TaskManager#execSyncTask(String, Task)
   */
  public ProgressQueue execSyncTask(String name, Task task){
    TaskOutputImpl out = new TaskOutputImpl(_queues, logger());
    _taskMan.execAsyncTask(name, task, out);
    return out.getProgressQueue();
  }

  /**
   * @see org.sapia.corus.taskmanager.TaskManager#execSyncTask(java.lang.String, org.sapia.taskman.Task, org.sapia.taskman.TaskOutput)
   */
  public void execSyncTask(String name, Task task, TaskOutput out){
    _taskMan.execSyncTask(name, task, out);
  }
  
  /**
   * @see org.sapia.corus.taskmanager.TaskManager#execAsyncTask(java.lang.String, org.sapia.taskman.Task, org.sapia.taskman.TaskOutput)
   */
  public void execAsyncTask(String name, Task task, TaskOutput out){
    _taskMan.execAsyncTask(name, task, out);
  }
  
  /**
   * @see org.sapia.corus.taskmanager.TaskManager#execAsyncTask(java.lang.String, org.sapia.taskman.Task)
   */
  public void execAsyncTask(String name, Task task){
    _taskMan.execAsyncTask(name, task, _taskMan.newTaskOutput(name));
  }
  
  /**
   * @see org.sapia.corus.taskmanager.TaskManager#execTaskFor(org.sapia.taskman.TaskDescriptor)
   */
  public void execTaskFor(TaskDescriptor td) {
    _taskMan.execTaskFor(td);
  }
  
  /**
   * @see org.sapia.corus.taskmanager.TaskManager#getProgressQueue(int)
   */
  public ProgressQueue getProgressQueue(int level){
  	ProgressQueue queue = new ProgressQueueImpl();
  	_queues.addProgressQueue(queue, level);
  	return queue;
  }
  
  /**
   * @return the number of pending tasks held by this instance.
   */
  public int getTaskCount() {
    return CorusTaskman.activeCount();
  }
}
