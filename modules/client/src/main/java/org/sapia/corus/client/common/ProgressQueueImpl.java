package org.sapia.corus.client.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the {@link ProgressQueue} interface.
 * 
 * @author yduchesne
 */
public class ProgressQueueImpl implements ProgressQueue, Externalizable {
  private List<ProgressMsg> msgs = new ArrayList<ProgressMsg>();
  private boolean          over;

  public ProgressQueueImpl() {
  }
  
  // --------------------------------------------------------------------------
  // ProgressQueue interface
  
  @Override
  public synchronized boolean hasNext() {
    while ((msgs.size() == 0) && !over) {
      try {
        wait();
      } catch (InterruptedException e) {
        return false;
      }
    }

    return msgs.size() > 0;
  }

  @Override
  public synchronized List<ProgressMsg> next() {
    List<ProgressMsg> toReturn = new ArrayList<ProgressMsg>(msgs);
    msgs.clear();

    return toReturn;
  }

  @Override
  public List<ProgressMsg> fetchNext() {
    if (hasNext()) {
      return next();
    }
    return new ArrayList<ProgressMsg>(0);
  }

  @Override
  public synchronized void addMsg(ProgressMsg msg) {
    if (!over || msg.isError()) {
      msgs.add(msg);
      handleMsg(msg);
      notify();
    }
  }

  @Override
  public synchronized void close() {
    over = true;
    notify();
  }

  @Override
  public boolean isClosed() {
    return over;
  }
  
  @Override
  public synchronized void debug(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.DEBUG));
  }

  @Override
  public synchronized void verbose(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.VERBOSE));
  }

  @Override
  public synchronized void info(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.INFO));
  }
  
  @Override
  public synchronized void error(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.ERROR));
  }

  @Override
  public synchronized void warning(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.WARNING));
  }

  @Override
  public List<ProgressMsg> waitFor() {
    List<ProgressMsg> toReturn = new ArrayList<ProgressMsg>();
    while (this.hasNext()) {
      List<ProgressMsg> batch = fetchNext();
      for (ProgressMsg m : batch) {
        if (m.isError()) {
          if (m.isThrowable()) {
            Throwable err = (Throwable) m.getMessage();
            throw new ProgressException("Error performing operation", err);
          } else {
            throw new ProgressException("Error performing operation: " + m.getMessage().toString());
          }
        }
        toReturn.add(m);
      }
    }
    return toReturn;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    msgs = (List<ProgressMsg>) in.readObject();
    over = in.readBoolean();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(msgs);
    out.writeBoolean(over);
  }
  
  // --------------------------------------------------------------------------
  // Restricted

  /**
   * This template method is internally called from within the
   * {@link #addMsg(ProgressMsg)} method, upon a message being added to an
   * instance of this class.
   * 
   * @param msg
   *          a newly added {@link ProgressMsg}
   */
  protected void handleMsg(ProgressMsg msg) {
  }
}
