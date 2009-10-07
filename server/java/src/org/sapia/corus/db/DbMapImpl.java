package org.sapia.corus.db;

import java.io.IOException;
import java.util.Iterator;

import jdbm.JDBMEnumeration;
import jdbm.JDBMHashtable;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DbMapImpl<K, V> implements DbMap<K, V> {
  private JDBMHashtable _hashtable;

  /**
   * Constructor for DbMapImpl.
   */
  DbMapImpl(JDBMHashtable hashtable) {
    _hashtable = hashtable;
  }

  /**
   * @see org.sapia.corus.db.DbMap#close()
   */
  public void close() {
    try {
      _hashtable.dispose();
    } catch (IOException e) {
      // noop;
    }
  }

  /**
   * @see org.sapia.corus.db.DbMap#get(Object)
   */
  public V get(K key) {
    try {
      return (V)_hashtable.get(key);
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

  /**
   * @see org.sapia.corus.db.DbMap#keys()
   */
  public Iterator<K> keys() {
    try {
      return new DbIterator(_hashtable.keys());
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

  /**
   * @see org.sapia.corus.db.DbMap#put(Object, Object)
   */
  public void put(Object key, Object value) {
    try {
      _hashtable.put(key, value);
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

  /**
   * @see org.sapia.corus.db.DbMap#remove(Object)
   */
  public void remove(Object key) {
    try {
      _hashtable.remove(key);
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

  /**
   * @see org.sapia.corus.db.DbMap#values()
   */
  public Iterator values() {
    try {
      return new DbIterator(_hashtable.values());
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

  public void clear() {
    Iterator<?> keys = keys();    
    while(keys.hasNext()){
      this.remove(keys.next());
    }
  }
  
  /*//////////////////////////////////////////////////
                    INNER CLASSES
  //////////////////////////////////////////////////*/
  public static class DbIterator<V> implements Iterator<V> {
    private JDBMEnumeration _enum;

    DbIterator(JDBMEnumeration anEnum) {
      _enum = anEnum;
    }

    public boolean hasNext() {
      try {
        return _enum.hasMoreElements();
      } catch (IOException e) {
        throw new IORuntimeException(e);
      }
    }

    public V next() {
      try {
        return (V)_enum.nextElement();
      } catch (IOException e) {
        throw new IORuntimeException(e);
      }
    }

    public void remove() {
      throw new UnsupportedOperationException("remove()");
    }
  }
}
