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
public class HashDbMap<K, V> implements DbMap<K, V> {
  private Map<K, V> _map = new HashMap<K, V>();

  public void close() {
  }

  public V get(K key) {
    return _map.get(key);
  }

  public Iterator<K> keys() {
    return _map.keySet().iterator();
  }

  public void put(K key, V value) {
    _map.put(key, value);
  }

  public void remove(K key) {
    _map.remove(key);
  }

  public Iterator<V> values() {
    return _map.values().iterator();
  }
  
  public void clear() {
    _map.clear();
  }
}
