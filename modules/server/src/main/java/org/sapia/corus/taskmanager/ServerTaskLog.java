package org.sapia.corus.taskmanager;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskLog;

public class ServerTaskLog implements TaskLog{
  
  private List<ProgressMsg>    _msgs = new ArrayList<ProgressMsg>();
  private ProgressQueues _queues;
  private TaskLog _delegate;
  
  public ServerTaskLog(ProgressQueues queues, TaskLog delegate){
    _queues = queues;
    _delegate = delegate;
  }
  
  public synchronized boolean hasNext() {
    while ((_msgs.size() == 0)) {
      try {
        wait();
      } catch (InterruptedException e) {
        return false;
      }
    }

    return _msgs.size() > 0;
  }

  public synchronized List<ProgressMsg> next() {
    List<ProgressMsg> toReturn = new ArrayList<ProgressMsg>(_msgs);
    _msgs.clear();
    return toReturn;
  }

  public synchronized void addMsg(ProgressMsg msg) {
    _msgs.add(msg);
    handleMsg(msg);
    _queues.notify(msg);
    notify();
  }

  public synchronized void close() {
    _delegate.close();
  }
  
  protected void handleMsg(ProgressMsg msg) {
  }
  
  public void debug(Task<?,?> task, String msg) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.DEBUG));
    _delegate.debug(task, msg);
  }

  public void info(Task<?,?> task, String msg) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.INFO));
    _delegate.info(task, msg);    
  }
  
  public void warn(Task<?,?> task, String msg, Throwable err) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.WARNING));
    _delegate.warn(task, msg, err);    
  }
  
  public void warn(Task<?,?> task, String msg) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.WARNING));
    _delegate.info(task, msg);    
  }

  public void error(Task<?,?> task, String msg) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.ERROR));
    _delegate.error(task, msg);    
  }
  
  public void error(Task<?,?> task, String msg, Throwable err) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.ERROR));
    _delegate.error(task, msg, err);    
  }
  
  private String format(Task<?,?> task, String msg){
    return task.getName() + " >> " + msg;
  }
  
  static class ServerProgressQueue implements ProgressQueue{
    
    ServerTaskLog _owner;
    
    public ServerProgressQueue(ServerTaskLog owner) {
      _owner = owner;
    }
    
    public boolean isClosed() {
      return false;
    }
    
    public boolean hasNext() {
      return _owner.hasNext();
    }

    public List<ProgressMsg> next() {
      return _owner.next();
    }
    public List<ProgressMsg> fetchNext() {
      synchronized(_owner._msgs){
        try{
          return new ArrayList<ProgressMsg>(_owner._msgs);         
        }finally{
          _owner._msgs.clear();
        }
      }
    }
    
    public void close() {
      _owner._delegate.close();
    }
    
    public void info(Object msg) {}    
    public void verbose(Object msg) {}
    public void warning(Object msg) {}
    public void debug(Object msg) {}
    public void error(Object msg) {}
    public void addMsg(ProgressMsg msg) {}
  }
}
