package org.sapia.corus.client.services.db.persistence;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.services.db.DbMap;

public abstract class AbstractPersistent<K, V> implements Persistent<K, V> {

  private volatile transient DbMap<K, V> db;

  private volatile boolean deleted;

  @Override
  @Transient
  public void setDbMap(DbMap<K, V> db) {
    this.db = db;
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public boolean save() {
    if (db != null && !deleted) {
      db.put(this.getKey(), (V) this);
      return true;
    }
    return false;
  }

  @Override
  public boolean delete() {
    if (db != null && !deleted) {
      db.remove(getKey());
      deleted = true;
      return true;
    }
    return false;
  }

  @Override
  public void markDeleted() {
    deleted = true;
    setDbMap(null);
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public boolean refresh() {
    if (db != null && !deleted) {
      db.refresh(getKey(), (V) this);
      return true;
    }
    return false;
  }

}
