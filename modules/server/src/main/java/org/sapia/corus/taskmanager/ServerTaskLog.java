package org.sapia.corus.taskmanager;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.common.ProgressException;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskLog;

public class ServerTaskLog implements TaskLog {

  private List<ProgressMsg> msgs = new ArrayList<ProgressMsg>();
  private ProgressQueues queues;
  private TaskLog delegate;

  public ServerTaskLog(ProgressQueues queues, TaskLog delegate) {
    this.queues = queues;
    this.delegate = delegate;
  }

  public synchronized boolean hasNext() {
    while (this.msgs.size() == 0) {
      try {
        wait();
      } catch (InterruptedException e) {
        return false;
      }
    }

    return msgs.size() > 0;
  }

  public synchronized List<ProgressMsg> next() {
    List<ProgressMsg> toReturn = new ArrayList<ProgressMsg>(msgs);
    msgs.clear();
    return toReturn;
  }

  public synchronized void addMsg(ProgressMsg msg) {
    msgs.add(msg);
    handleMsg(msg);
    queues.notify(msg);
    notify();
  }

  public synchronized void close() {
    delegate.close();
  }

  protected void handleMsg(ProgressMsg msg) {
  }

  public void debug(Task<?, ?> task, String msg) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.DEBUG));
    delegate.debug(task, msg);
  }

  public void info(Task<?, ?> task, String msg) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.INFO));
    delegate.info(task, msg);
  }

  public void warn(Task<?, ?> task, String msg, Throwable err) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.WARNING));
    delegate.warn(task, msg, err);
  }

  public void warn(Task<?, ?> task, String msg) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.WARNING));
    delegate.info(task, msg);
  }

  public void error(Task<?, ?> task, String msg) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.ERROR));
    delegate.error(task, msg);
  }

  public void error(Task<?, ?> task, String msg, Throwable err) {
    addMsg(new ProgressMsg(format(task, msg), ProgressMsg.ERROR));
    delegate.error(task, msg, err);
  }

  private String format(Task<?, ?> task, String msg) {
    return task.getName() + " >> " + msg;
  }

  static class ServerProgressQueue implements ProgressQueue {

    ServerTaskLog owner;

    public ServerProgressQueue(ServerTaskLog owner) {
      this.owner = owner;
    }

    public boolean isClosed() {
      return false;
    }

    public boolean hasNext() {
      return owner.hasNext();
    }

    public List<ProgressMsg> next() {
      return owner.next();
    }

    public List<ProgressMsg> fetchNext() {
      synchronized (owner.msgs) {
        try {
          return new ArrayList<ProgressMsg>(owner.msgs);
        } finally {
          owner.msgs.clear();
        }
      }
    }

    @Override
    public List<ProgressMsg> waitFor() throws ProgressException {
      return new ArrayList<ProgressMsg>(owner.msgs);
    }

    public void close() {
      owner.delegate.close();
    }

    public void info(Object msg) {
    }

    public void verbose(Object msg) {
    }

    public void warning(Object msg) {
    }

    public void debug(Object msg) {
    }

    public void error(Object msg) {
    }

    public void addMsg(ProgressMsg msg) {
    }
  }
}
