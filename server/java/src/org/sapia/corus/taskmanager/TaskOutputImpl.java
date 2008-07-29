package org.sapia.corus.taskmanager;

import java.util.ArrayList;
import java.util.List;

import org.apache.log.Logger;
import org.sapia.corus.util.ProgressMsg;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.taskman.TaskOutput;

/**
 * Implements the <code>TaskOutput</code> interface.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TaskOutputImpl implements TaskOutput{

  private List    _msgs = new ArrayList();
  private boolean _over;
  private String  _taskName;
  private Logger  _logger;
  private ProgressQueues _queues;

  TaskOutputImpl(ProgressQueues queues, Logger logger){
    _logger = logger;
    _queues = queues;
  }
  
  public synchronized boolean hasNext() {
    while ((_msgs.size() == 0) && !_over) {
      try {
        wait();
      } catch (InterruptedException e) {
        return false;
      }
    }

    return _msgs.size() > 0;
  }

  public synchronized List next() {
    List toReturn = new ArrayList(_msgs);
    _msgs.clear();
    return toReturn;
  }

  public synchronized void addMsg(ProgressMsg msg) {
    if (_over) {
      return;
    }
    _msgs.add(msg);
    handleMsg(msg);
    _queues.notify(msg);
    notify();
  }

  public synchronized void close() {
    _over = true;
    notify();
  }

  /**
   * @see org.sapia.corus.util.ProgressQueue#debug(Object)
   */
  public synchronized TaskOutput debug(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.DEBUG));
    _logger.debug(taskName() + msg.toString());    
    return this;
  }

  /**
   * @see org.sapia.corus.util.ProgressQueue#verbose(Object)
   */
  public synchronized TaskOutput info(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.INFO));
    _logger.info(taskName() + msg.toString());
    return this;
  }

  /**
   * @see org.sapia.corus.util.ProgressQueue#error(Object)
   */
  public synchronized TaskOutput error(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.ERROR));
    _logger.error(taskName() + msg.toString());    
    return this;
  }

  /**
   * @see org.sapia.corus.util.ProgressQueue#warning(Object)
   */
  public synchronized TaskOutput warning(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.WARNING));
    _logger.warn(taskName() + msg.toString());    
    return this;
  }

  protected void handleMsg(ProgressMsg msg) {
  }
  /**
   * @see org.sapia.taskman.TaskOutput#error(java.lang.Object, java.lang.Throwable)
   */
  public TaskOutput error(Object msg, Throwable err) {
    addMsg(new ProgressMsg(err, ProgressMsg.ERROR));
    _logger.error(taskName() + msg.toString(), err);    
    return this;
  }

  /**
   * @see org.sapia.taskman.TaskOutput#error(java.lang.Throwable)
   */
  public TaskOutput error(Throwable err) {
    addMsg(new ProgressMsg(err, ProgressMsg.ERROR));
    _logger.error(taskName() + "Error caught", err);    
    return this;
  }
  
  /**
   * @return a <code>ProgressQueue</code> that in fact receives data from
   * this instance.
   */
  public ProgressQueue getProgressQueue(){
    return new ProgressQueueOutput(this);
  }

  /**
   * @see org.sapia.taskman.TaskOutput#setTaskName(java.lang.String)
   */
  public void setTaskName(String name) {
    _taskName = name;
  }
  
  protected String taskName(){
    return _taskName != null ? _taskName + " >> " : "";
  }
  
  /*//////////////////////////////////////////////////////////
                          INNER CLASSES 
  //////////////////////////////////////////////////////////*/
  
  public static class ProgressQueueOutput implements ProgressQueue{
    
    private TaskOutputImpl _impl;
    
    public ProgressQueueOutput(TaskOutputImpl impl){
      _impl = impl;
    }
    
    public boolean hasNext() {
      return _impl.hasNext();
    }

    public List next() {
      return _impl.next();
    }
  
    public void addMsg(ProgressMsg msg) {}
    public void close() {
    }
    public boolean isClosed() {
      return _impl._over;
    }
    public List fetchNext() {
      synchronized(_impl){
      	try{
					return _impl._msgs;      		
      	}finally{
      		_impl._msgs.clear();
      	}
      }
    }
    public void info(Object msg) {}    
    public void verbose(Object msg) {}
    public void warning(Object msg) {}
    public void debug(Object msg) {}
    public void error(Object msg) {}
  }

}
