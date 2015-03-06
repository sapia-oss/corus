package org.sapia.corus.database;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.database.PersistentDb;

public class TestDbModule{
  
  private PersistentDb db;

  public void setup() throws IOException{
    db = PersistentDb.open(System.getProperty("java.io.tmpdir")
          + File.separator + "testdb" + UUID.randomUUID());
  }
  
  public void teardown() {
    db.close();
  }
  
  public <K, V>  DbMap<K, V> createDbMap(
      String name, 
      Class<K> keyType,
      Class<V> valueType) throws IOException{
    return db.getDbMap(keyType, valueType, name);
  }
 
}
