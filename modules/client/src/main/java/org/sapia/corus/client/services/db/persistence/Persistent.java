package org.sapia.corus.client.services.db.persistence;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.services.db.DbMap;

/**
 * Implemented by persistent objects.
 * 
 * @author yduchesne
 *
 * @param <K> the generic type of the persistent object key.
 * @param <V> the generic type of th persistent object value.
 */
public interface Persistent<K, V> {

  @Transient
  public K getKey();
  
  public void setDbMap(DbMap<K, V> db);
  
  public boolean save();
  
  public boolean refresh();
}
