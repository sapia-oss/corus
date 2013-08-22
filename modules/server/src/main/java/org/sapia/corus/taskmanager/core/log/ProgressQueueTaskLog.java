package org.sapia.corus.taskmanager.core.log;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.common.ProgressException;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskLog;

/**
 * A {@link ProgressQueue} implementation that is backed by a {@link TaskLog}.
 * 
 * @author yduchesne
 *
 */
public class ProgressQueueTaskLog implements ProgressQueue{

  private Task<?,?> task;
  private TaskLog 	log;
  private volatile boolean closed;
  
  public ProgressQueueTaskLog(Task<?,?> task, TaskLog log) {
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

  public List<ProgressMsg> fetchNext() {
    return new ArrayList<ProgressMsg>(0);
  }

  public boolean hasNext() {
    return false;
  }

  public synchronized boolean isClosed() {
    return closed;
  }

  public List<ProgressMsg> next() {
    return new ArrayList<ProgressMsg>(0);
  }
  
  @Override
  public List<ProgressMsg> waitFor() throws ProgressException {
    return new ArrayList<ProgressMsg>();
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
