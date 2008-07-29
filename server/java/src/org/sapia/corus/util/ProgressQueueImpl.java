package org.sapia.corus.util;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ProgressQueueImpl implements ProgressQueue {
  private List    _msgs = new ArrayList();
  private boolean _over;

  /**
   * @see org.sapia.corus.util.ProgressQueue#hasNext()
   */
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
  
  /**
   * @see org.sapia.corus.util.ProgressQueue#next()
   */
  public synchronized List next() {
    List toReturn = new ArrayList(_msgs);
    _msgs.clear();

    return toReturn;
  }
  
  /**
   * @see org.sapia.corus.util.ProgressQueue#fetchNext()
   */
  public List fetchNext() {
		if(hasNext()){
			return next();			
		}
		return new ArrayList(0);
  }

  /**
   * @see org.sapia.corus.util.ProgressQueue#addMsg(org.sapia.corus.util.ProgressMsg)
   */
  public synchronized void addMsg(ProgressMsg msg) {
    if (_over) {
      throw new IllegalStateException("progress queue is closed; cannot accept more input");
    }

    _msgs.add(msg);
    handleMsg(msg);
    notify();
  }
  
  /**
   * @see org.sapia.corus.util.ProgressQueue#close()
   */
  public synchronized void close() {
    _over = true;
    notify();
  }
  
  /**
   * 
   * @see org.sapia.corus.util.ProgressQueue#isClosed()
   */
  public boolean isClosed(){
  	return _over;
  }

  /**
   * @see org.sapia.corus.util.ProgressQueue#debug(Object)
   */
  public synchronized void debug(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.DEBUG));
  }

  /**
   * @see org.sapia.corus.util.ProgressQueue#verbose(Object)
   */
  public synchronized void verbose(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.VERBOSE));
  }

  /**
   * @see org.sapia.corus.util.ProgressQueue#verbose(Object)
   */
  public synchronized void info(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.INFO));
  }

  /**
   * @see org.sapia.corus.util.ProgressQueue#error(Object)
   */
  public synchronized void error(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.ERROR));
  }

  /**
   * @see org.sapia.corus.util.ProgressQueue#warning(Object)
   */
  public synchronized void warning(Object msg) {
    addMsg(new ProgressMsg(msg, ProgressMsg.WARNING));
  }

  protected void handleMsg(ProgressMsg msg) {
  }
}
