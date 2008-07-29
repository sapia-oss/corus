package org.sapia.corus.db;

import java.util.Iterator;


/**
 * Specifies behavior similar to the <code>java.util.Map</code>
 * interface. Instances of this interface are expected to provide
 * persistency.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface DbMap {
  /**
   * Puts the an object in this map, mapped to the given key.
   *
   * @param key the key of the passed in value.
   * @param value the value to persist.
   */
  public void put(Object key, Object value);

  /**
   * Removes the object that corresponds to the given key.
   *
   * @param key the key for which the corresponding object should be
   * removed.
   */
  public void remove(Object key);

  /**
   * Returns an iterator of this instance's values.
   *
   * @return an <code>Iterator</code>.
   */
  public Iterator values();

  /**
   * Returns an iterator of this instance's keys.
   *
   * @return an <code>Iterator</code>.
   */
  public Iterator keys();

  /**
   * Closes this instance - releases all resources held by it.
   */
  public void close();

  /**
   * Return the object for the given key.
   *
   * @param key a key for which to return the corresponding object.
   * @return returns the object corresponding to the passed in key,
   * or <code>null</code> if no object could be found.
   */
  public Object get(Object key);
}
