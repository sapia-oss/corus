package org.sapia.corus.database.persistence;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.annotations.Version;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.persistence.Persistent;

public class TestPersistentObject implements Persistent<String, TestPersistentObject> {

  private long id = System.currentTimeMillis();
  private String name;
  private boolean active = true;
  private long version;

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public long getId() {
    return id;
  }

  void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void refresh() {
  }

  @Override
  public String getKey() {
    return name;
  }

  @Override
  public void save() {
  }

  @Override
  public void delete() {
  }
  
  @Override
  public void markDeleted() {
  }
  
  @Override
  @Transient
  public boolean isDeleted() {
    return false;
  }
  
  @Override
  public void recycle() throws IllegalArgumentException {
  }

  @Override
  public void setDbMap(DbMap<String, TestPersistentObject> db) {
  }

  void setVersion(long version) {
    this.version = version;
  }

  @Version
  public long getVersion() {
    return version;
  }

}
