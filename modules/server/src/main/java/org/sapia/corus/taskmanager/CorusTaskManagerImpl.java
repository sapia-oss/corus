package org.sapia.corus.taskmanager;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.FutureResult;
import org.sapia.corus.taskmanager.core.SequentialTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskConfig;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.taskmanager.core.TaskManagerImpl;
import org.sapia.corus.taskmanager.core.log.LoggerTaskLog;
import org.sapia.ubik.rmi.Remote;


/**
 * This module implements the <code>TaskManager</code> interface.
 * 
 * @author Yanick Duchesne
 */
@Bind(moduleInterface=CorusTaskManager.class)
@Remote(interfaces=CorusTaskManager.class)
public class CorusTaskManagerImpl extends ModuleHelper implements CorusTaskManager{
  
  private TaskManager _delegate; 
  private ProgressQueues _queues = new ProgressQueues();

  /*////////////////////////////////////////////////////////////////////
                        Service INTERFACE METHODS
  ////////////////////////////////////////////////////////////////////*/

  public void init() throws Exception {
    _delegate = new TaskManagerImpl(
        new ServerTaskLog(_queues, new LoggerTaskLog(_logger)), serverContext());
  }

  public void dispose() {
  }

  /*////////////////////////////////////////////////////////////////////
                         Module INTERFACE METHODS
  ////////////////////////////////////////////////////////////////////*/

  /**
   * @see org.sapia.corus.client.Module#getRoleName()
   */
  public String getRoleName() {
    return CorusTaskManager.ROLE;
  }

  /*////////////////////////////////////////////////////////////////////
                       TaskManager INTERFACE METHODS
  ////////////////////////////////////////////////////////////////////*/
  
  public void execute(Task task) {
    _delegate.execute(task);
  }
  
  public void execute(Task task, SequentialTaskConfig conf) {
    _delegate.execute(task, conf);
  }
  
  public FutureResult executeAndWait(Task task) {
    return _delegate.executeAndWait(task);
  }
  
  public FutureResult executeAndWait(Task task, TaskConfig cfg) {
    return _delegate.executeAndWait(task, cfg);
  }
  
  public void executeBackground(Task task, BackgroundTaskConfig cfg) {
    _delegate.executeBackground(task, cfg);
  }

  public ProgressQueue getProgressQueue(int level){
  	ProgressQueue queue = new ProgressQueueImpl();
  	_queues.addProgressQueue(queue, level);
  	return queue;
  }

}
