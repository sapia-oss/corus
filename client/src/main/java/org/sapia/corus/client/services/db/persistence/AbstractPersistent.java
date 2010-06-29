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
  public void save() {
    if(db == null){
      throw new IllegalStateException("DB Map not set; cannot save");
    }
    db.put(this.getKey(), (V)this);
  }
  
  @Override
  @SuppressWarnings(value="unchecked")
  public void refresh() {
    if(db == null){
      throw new IllegalStateException("DB Map not set; cannot refresh");
    }
    db.refresh(getKey(), (V)this);
  }

}
