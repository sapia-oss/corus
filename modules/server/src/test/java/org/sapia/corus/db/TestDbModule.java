package org.sapia.corus.db;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.sapia.corus.client.services.db.DbMap;

public class TestDbModule{
  
  private JdbmDb db;

  public void setup() throws IOException{
    db = JdbmDb.open(System.getProperty("java.io.tmpdir")
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
