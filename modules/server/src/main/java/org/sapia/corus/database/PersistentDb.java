package org.sapia.corus.database;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentNavigableMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.persistence.Record;
import org.sapia.corus.database.DbMapImpl.TxFacade;

/**
 * Wraps a {@link DB} instance.
 * 
 * @author Yanick Duchesne
 */
public class PersistentDb {
  
  private static final int CACHE_SIZE = 1000;
  
  private DB db;

  private PersistentDb(DB db) {
    this.db = db;
  }

  <K, V> DbMap<K, V> getDbMap(Class<K> keyType, Class<V> valueType, String name) throws IOException {
    ConcurrentNavigableMap<K, Record<V>> treeMap = db.getTreeMap(name);
    TxFacade txFacade = new TxFacade() {
      @Override
      public void commit() {
        db.commit();
      }
    };
    return new DbMapImpl<K, V>(keyType, valueType, txFacade, treeMap);
  }

  static PersistentDb open(String fName) throws IOException {
    DB db = DBMaker.newFileDB(new File(fName))
        .closeOnJvmShutdown()
        .cacheLRUEnable()
        .cacheSize(CACHE_SIZE)
        .make();
    return new PersistentDb(db);
  }

  void close() {
    try {
      db.close();
    } catch (Exception e) {
      // noop;
    }
  }
}
