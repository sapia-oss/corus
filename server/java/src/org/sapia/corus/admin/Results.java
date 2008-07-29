package org.sapia.corus.admin;

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
public class Results {
  private List    _hostLists  = new ArrayList();
  private boolean _incomplete = true;

  /**
   * Constructor for ResultList.
   */
  public Results() {
  }

  synchronized void addResult(HostList list) {
    _hostLists.add(list);
    notify();
  }

  synchronized void addResult(HostItem item) {
    _hostLists.add(item);
    notify();
  }
  
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

  public Object next() {
    return _hostLists.remove(0);
  }

  synchronized void complete() {
    _incomplete = false;
    notifyAll();
  }
}
