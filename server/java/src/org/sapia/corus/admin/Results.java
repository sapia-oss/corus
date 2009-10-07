package org.sapia.corus.admin;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class aggregates {@link HostItem}s and {@link HostList}s.
 * 
 * @author Yanick Duchesne
 */
//@SuppressWarnings
public class Results {
  private List    _hostLists  = new ArrayList();
  private boolean _incomplete = true;

  /**
   * @param list a {@link HostList}
   */
  synchronized void addResult(HostList list) {
    _hostLists.add(list);
    notify();
  }

  /**
   * @param item a {@link HostItem}
   */
  synchronized void addResult(HostItem item) {
    _hostLists.add(item);
    notify();
  }
  
  /**
   * @return <code>true</code> if this instance contains other objects.
   */
  public synchronized boolean hasNext() {
    if (_hostLists.size() > 0) {
      return true;
    }

    while (_incomplete) {
      try {
        wait();
      } catch (InterruptedException e) {
        return false;
      }
    }

    return _hostLists.size() > 0;
  }

  /**
   * @return the next object that this instance contains (the returned object
   * is removed from this instance).
   */
  public Object next() {
    return _hostLists.remove(0);
  }

  synchronized void complete() {
    _incomplete = false;
    notifyAll();
  }
}
