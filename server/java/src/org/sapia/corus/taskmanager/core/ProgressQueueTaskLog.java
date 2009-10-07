package org.sapia.corus.taskmanager.core;

import java.util.Collections;
import java.util.List;

import org.sapia.corus.util.ProgressMsg;
import org.sapia.corus.util.ProgressQueue;

public class ProgressQueueTaskLog implements ProgressQueue{

  private Task  task;
  private TaskLog log;
  private volatile boolean closed;
  
  public ProgressQueueTaskLog(Task task, TaskLog log) {
    this.task = task;
    this.log  = log;
  }

  public void addMsg(ProgressMsg msg) {
    if(msg.getStatus() == ProgressMsg.VERBOSE){
      debug(msg.getMessage());
    }
    else if(msg.getStatus() == ProgressMsg.DEBUG){
      debug(msg.getMessage());
    }
    else if(msg.getStatus() == ProgressMsg.INFO){
      info(msg.getMessage());
    }
    else if(msg.getStatus() == ProgressMsg.WARNING){
      warning(msg.getMessage());
    }
    else if(msg.getStatus() == ProgressMsg.ERROR){
      error(msg.getMessage());
    }
  }

  public void close() {
    closed = true;
  }

  public void verbose(Object msg) {
    log.debug(task, asString(msg));
  }

  public void debug(Object msg) {
    log.debug(task, asString(msg));
  }
  
  public void info(Object msg) {
    log.info(task, asString(msg));
  }

  public void warning(Object msg) {
    log.warn(task, asString(msg));
  }

  public void error(Object msg) {
    log.error(task, asString(msg));
  }

  public List fetchNext() {
    return Collections.EMPTY_LIST;
  }

  public boolean hasNext() {
    return false;
  }


  public synchronized boolean isClosed() {
    return closed;
  }

  public List next() {
    return Collections.EMPTY_LIST;
  }
  
  private String asString(Object msg){
    if(msg == null){
      return "null";
    }
    else if(msg instanceof String){
      return (String)msg;
    }
    else{
      return msg.toString();
    }
    
  }

}
