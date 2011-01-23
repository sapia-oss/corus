package org.sapia.corus.client.services.db;

import java.util.Collection;
import java.util.Iterator;

import org.sapia.corus.client.services.db.persistence.ClassDescriptor;


/**
 * Specifies behavior similar to the <code>java.util.Map</code>
 * interface. Instances of this interface are expected to provide
 * persistency.
 *
 * @author Yanick Duchesne
 */
public interface DbMap<K, V> {
  
  /**
   * @return the {@link ClassDescriptor} corresponding to the type of object that this instance accepts. 
   */
  public ClassDescriptor<V> getClassDescriptor();
  
  
  /**
   * Puts the an object in this map, mapped to the given key.
   *
   * @param key the key of the passed in value.
   * @param value the value to persist.
   */
  public void put(K key, V value);
  
  /**
   * Refreshes the given value: assigns to it the state that is
   * currently persisted in this instance.
   * 
   * @param key the key of the passed in value.
   * @param value the value whose state is to be refreshed.
   */
  public void refresh(K key, V value);

  /**
   * Removes the object that corresponds to the given key.
   *
   * @param key the key for which the corresponding object should be
   * removed.
   */
  public void remove(K key);

  /**
   * Returns an iterator of this instance's values.
   *
   * @return an <code>Iterator</code>.
   */
  public Iterator<V> values();

  /**
   * Returns an iterator of this instance's keys.
   *
   * @return an <code>Iterator</code>.
   */
  public Iterator<K> keys();

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
  public V get(K key);
  
  /**
   * Clears this instance's values.
   */
  public void clear();
  
  /**
   * @param template the object that serves as a template for matching.
   * @return the {@link RecordMatcher} corresponding to the given template object.
   */
  public RecordMatcher<V> createMatcherFor(V template);

  /**
   * 
   * @param matcher the {@link RecordMatcher} to use to filter the values that will be returned.
   * @return the {@link Collection} of values that were deemed a match.
   */
  public Collection<V> values(RecordMatcher<V> matcher);
}
