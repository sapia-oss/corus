package org.sapia.corus.taskmanager;

import org.apache.log.Logger;
import org.sapia.corus.util.ProgressMsg;
import org.sapia.taskman.TaskOutput;

/**
 * Implements the <code>TaskOutput</code> interface over a <code>Logger</code>.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TaskOutputLogger implements TaskOutput{
  
  private Logger _logger;
  private String _taskName;
  private ProgressQueues _queues;
  
  TaskOutputLogger(ProgressQueues queues, Logger logger){
    _logger = logger;
    _queues = queues;
  }
  
  /**
   * @see org.sapia.taskman.TaskOutput#close()
   */
  public void close() {}

  /**
   * @see org.sapia.taskman.TaskOutput#debug(java.lang.Object)
   */
  public TaskOutput debug(Object msg) {
  	String content;
    _logger.debug(content = taskName() + msg.toString());
    notify(new ProgressMsg(content, ProgressMsg.DEBUG));
    return this;
  }

  /**
   * @see org.sapia.taskman.TaskOutput#error(java.lang.Object, java.lang.Throwable)
   */
  public TaskOutput error(Object msg, Throwable err) {
    _logger.error(taskName() + msg.toString(), err);
		notify(new ProgressMsg(err, ProgressMsg.ERROR));    
    return this;
  }

  /**
   * @see org.sapia.taskman.TaskOutput#error(java.lang.Object)
   */
  public TaskOutput error(Object err) {
    if(err instanceof Throwable){
      _logger.error(taskName() + "Error caught...", (Throwable)err);
			notify(new ProgressMsg(err, ProgressMsg.ERROR));      
    }
    else{
			String content;    	
      _logger.error(content = taskName() + err.toString());
			notify(new ProgressMsg(content, ProgressMsg.ERROR));      
    }
    
    return this;
  }

  /**
   * @see org.sapia.taskman.TaskOutput#error(java.lang.Throwable)
   */
  public TaskOutput error(Throwable err) {
    _logger.error(taskName() + "Error caught...", err);
		notify(new ProgressMsg(err, ProgressMsg.ERROR));    
    return this;
  }

  /**
   * @see org.sapia.taskman.TaskOutput#info(java.lang.Object)
   */
  public TaskOutput info(Object msg) {
		String content;  	
    _logger.info(content = taskName() + msg.toString());
		notify(new ProgressMsg(content, ProgressMsg.INFO));    
    return this;
  }

  /**
   * @see org.sapia.taskman.TaskOutput#warning(java.lang.Object)
   */
  public TaskOutput warning(Object msg) {
		String content;  	  	
    _logger.warn(content = taskName() + msg.toString());
		notify(new ProgressMsg(content, ProgressMsg.WARNING));    
    return this;
  }

  /**
   * @see org.sapia.taskman.TaskOutput#setTaskName(java.lang.String)
   */
  public void setTaskName(String name) {
    _taskName = name;
  }
  
  private void notify(ProgressMsg msg){
  	_queues.notify(msg);
  }

  
  String taskName(){
    return _taskName != null ? _taskName + " >> " : "";
  }

}
