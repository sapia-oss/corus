package org.sapia.corus.db;

import jdbm.JDBMEnumeration;
import jdbm.JDBMHashtable;

import java.io.IOException;

import java.util.Iterator;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DbMapImpl implements DbMap {
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
  public Object get(Object key) {
    try {
      return _hashtable.get(key);
    } catch (IOException e) {
      throw new IORuntimeException(e);
    }
  }

  /**
   * @see org.sapia.corus.db.DbMap#keys()
   */
  public Iterator keys() {
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

  /*//////////////////////////////////////////////////
                    INNER CLASSES
  //////////////////////////////////////////////////*/
  public static class DbIterator implements Iterator {
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

    public Object next() {
      try {
        return _enum.nextElement();
      } catch (IOException e) {
        throw new IORuntimeException(e);
      }
    }

    public void remove() {
      throw new UnsupportedOperationException("remove()");
    }
  }
}
