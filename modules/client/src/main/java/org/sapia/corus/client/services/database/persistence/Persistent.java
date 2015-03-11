package org.sapia.corus.client.services.database.persistence;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.services.database.DbMap;

/**
 * Implemented by persistent objects.
 * 
 * @author yduchesne
 * 
 * @param <K>
 *          the generic type of the persistent object key.
 * @param <V>
 *          the generic type of th persistent object value.
 */
public interface Persistent<K, V> {

  @Transient
  public K getKey();

  public void setDbMap(DbMap<K, V> db);

  public void save() throws IllegalArgumentException;

  public void recycle() throws IllegalArgumentException;
  
  public void delete() throws IllegalArgumentException;
  
  public boolean isDeleted();

  public void markDeleted() throws IllegalArgumentException;

  public void refresh();
}
