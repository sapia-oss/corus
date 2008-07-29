package org.sapia.corus.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HashDbMap implements DbMap {
  private Map _map = new HashMap();

  public void close() {
  }

  public Object get(Object key) {
    return _map.get(key);
  }

  public Iterator keys() {
    return _map.keySet().iterator();
  }

  public void put(Object key, Object value) {
    _map.put(key, value);
  }

  public void remove(Object key) {
    _map.remove(key);
  }

  public Iterator values() {
    return _map.values().iterator();
  }
}
