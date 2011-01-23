package org.sapia.corus.db.persistence;

import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.persistence.Persistent;

public class TestPersistentObject implements Persistent<String, TestPersistentObject>{
  
  private long id = System.currentTimeMillis();
  private String name;
  private boolean active = true;
  
  public boolean isActive(){
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
  public void setDbMap(DbMap<String, TestPersistentObject> db) {
  }
  
  
}
