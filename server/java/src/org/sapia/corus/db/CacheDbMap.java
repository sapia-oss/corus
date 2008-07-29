package org.sapia.corus.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CacheDbMap implements DbMap{
	
	private DbMap _db;
	private Map _cache = new HashMap(500);
	
	CacheDbMap(DbMap cached){
		_db = cached;
	}

  /**
   * @see org.sapia.corus.db.DbMap#close()
   */
  public synchronized void close() {
    _db.close();
  }

  /**
   * @see org.sapia.corus.db.DbMap#get(java.lang.Object)
   */
  public synchronized Object get(Object key) {
    Object toReturn = _cache.get(key);
    if(toReturn == null){
    	toReturn = _db.get(key);
    	if(toReturn != null){
    		_cache.put(key, toReturn);
    	}
    }
    return toReturn;
  }

  /**
   * @see org.sapia.corus.db.DbMap#keys()
   */
  public Iterator keys() {
    return _db.keys();
  }

  /**
   * @see org.sapia.corus.db.DbMap#put(java.lang.Object, java.lang.Object)
   */
  public synchronized void put(Object key, Object value) {
    _cache.remove(key);
		_db.put(key, value);
  }

  /**
   * @see org.sapia.corus.db.DbMap#remove(java.lang.Object)
   */
  public synchronized void remove(Object key) {
  	_cache.remove(key);
  	_db.remove(key);
  }

  /**
   * @see org.sapia.corus.db.DbMap#values()
   */
  public Iterator values() {
    return _db.values();
  }

}
