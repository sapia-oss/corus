package org.sapia.corus.taskmanager;

import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.taskmanager.core.ProgressBuffer;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskLog;

class ServerTaskLog implements TaskLog {

  private ProgressQueues    queues;
  private ProgressBuffer    buffer;
  private TaskLog           delegate;

  ServerTaskLog(ProgressBuffer buffer, ProgressQueues queues, TaskLog delegate) {
    this.buffer   = buffer;
    this.queues   = queues;
    this.delegate = delegate;
  }

  @Override
  public synchronized void close() {
    delegate.close();
  }
  
  @Override
  public void debug(Task<?, ?> task, String msg) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.DEBUG));
    delegate.debug(task, msg);
  }

  @Override
  public void info(Task<?, ?> task, String msg) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.INFO));
    delegate.info(task, msg);
  }

  @Override
  public void warn(Task<?, ?> task, String msg, Throwable err) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.WARNING));
    delegate.warn(task, msg, err);
  }

  @Override
  public void warn(Task<?, ?> task, String msg) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.WARNING));
    delegate.info(task, msg);
  }

  @Override
  public void error(Task<?, ?> task, String msg) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.ERROR));
    delegate.error(task, msg);
  }

  @Override
  public void error(Task<?, ?> task, String msg, Throwable err) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.ERROR));
    delegate.error(task, msg, err);
  }

  private String format(Task<?, ?> task, String msg) {
    return task.getName() + " >> " + msg;
  }
  
  private synchronized void addMsg(ProgressMsg msg) {
    buffer.add(msg);
    queues.notify(msg);
  }

}
