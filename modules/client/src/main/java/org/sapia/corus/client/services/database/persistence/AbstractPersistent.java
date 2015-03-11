package org.sapia.corus.client.services.database.persistence;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.services.database.DbMap;

public abstract class AbstractPersistent<K, V> implements Persistent<K, V> {

  private volatile transient DbMap<K, V> db;

  private volatile transient boolean deleted;

  @Override
  @Transient
  public void setDbMap(DbMap<K, V> db) {
    this.db = db;
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public void save() {
    if (deleted) {
      throw new IllegalStateException(getClass().getSimpleName() + "(" + getKey() 
          + ") deleted. Cannot save a deleted object. Invoke recycle() to reuse a deleted object");
    }
    if (db == null) {
      throw new IllegalStateException(getClass().getSimpleName() + "(" + getKey() 
          + ") is not associated to DB.");
    }
     db.put(this.getKey(), (V) this);
  }
  
  @Override
  @SuppressWarnings(value = "unchecked")
  public void recycle() {
    if (db == null) {
      throw new IllegalStateException(getClass().getSimpleName() + "(" + getKey() 
          + ") is not associated to DB.");
    }
    deleted = false;
    db.put(this.getKey(), (V) this);
  }


  @Override
  public void delete() {
    if (deleted) {
      throw new IllegalStateException(getClass().getSimpleName() + "(" + getKey() 
          + ") already deleted.");
    }
    if (db == null) {
      throw new IllegalStateException(getClass().getSimpleName() + "(" + getKey() 
          + ") is not associated to DB.");
    }
    db.remove(getKey());
    deleted = true;
  }
  
  @Override
  @Transient
  public boolean isDeleted() {
    return deleted;
  }

  @Override
  public void markDeleted() {
    deleted = true;
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public void refresh() {
    if (deleted) {
      throw new IllegalStateException(getClass().getSimpleName() + "(" + getKey() 
          + ") deleted");
    }
    if (db == null) {
      throw new IllegalStateException(getClass().getSimpleName() + "(" + getKey() 
          + ") is not associated to DB.");
    }
    db.refresh(getKey(), (V) this);
  }

}
