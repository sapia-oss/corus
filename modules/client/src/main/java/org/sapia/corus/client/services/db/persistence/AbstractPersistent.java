package org.sapia.corus.client.services.db.persistence;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.services.db.DbMap;

public abstract class AbstractPersistent<K, V> implements Persistent<K, V>{
  
  private transient DbMap<K, V> db;
  
  @Override
  @Transient
  public void setDbMap(DbMap<K, V> db) {
    this.db = db;
  }
  
  @Override
  @SuppressWarnings(value="unchecked")
  public boolean save() {
    if(db != null){
      db.put(this.getKey(), (V)this);
      return true;
    }
    return false;
  }
  
  @Override
  @SuppressWarnings(value="unchecked")
  public boolean refresh() {
    if(db != null){
      db.refresh(getKey(), (V)this);
      return true;
    }
    return false;
  }

}
