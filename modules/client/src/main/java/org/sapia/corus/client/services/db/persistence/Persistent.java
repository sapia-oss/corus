package org.sapia.corus.client.services.db.persistence;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.services.db.DbMap;

public interface Persistent<K, V> {

  @Transient
  public K getKey();
  
  public void setDbMap(DbMap<K, V> db);
  
  public void save();
  
  public void refresh();
}
