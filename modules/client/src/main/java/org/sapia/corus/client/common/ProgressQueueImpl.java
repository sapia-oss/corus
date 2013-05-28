package org.sapia.corus.client.common;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the {@link ProgressQueue} interface.
 * 
 * @author Yanick Duchesne
 */
public class ProgressQueueImpl implements ProgressQueue {
  private List<ProgressMsg> _msgs = new ArrayList<ProgressMsg>();
  private boolean           _over;

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
  
  public synchronized List<ProgressMsg> next() {
    List<ProgressMsg> toReturn = new ArrayList<ProgressMsg>(_msgs);
    _msgs.clear();

    return toReturn;
  }
  
  public List<ProgressMsg> fetchNext() {
		if(hasNext()){
			return next();			
		}
		return new ArrayList<ProgressMsg>(0);
  }

  public synchronized void addMsg(ProgressMsg msg) {
    if (!_over) {
      _msgs.add(msg);
      handleMsg(msg);
      notify();
    }
  }
  
  public synchronized void close() {
    _over = true;
    notify();
  }
  
  public boolean isClosed(){
  	return _over;
  }

  public synchronized void debug(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.DEBUG));
  }

  public synchronized void verbose(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.VERBOSE));
  }

  public synchronized void info(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.INFO));
  }

  public synchronized void error(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.ERROR));
  }

  public synchronized void warning(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.WARNING));
  }

  /**
   * This template method is internally called from within the {@link #addMsg(ProgressMsg)} method, 
   * upon a message being added to an instance of this class.
   * 
   * @param msg a newly added {@link ProgressMsg}
   */
  protected void handleMsg(ProgressMsg msg) {
  }
}
