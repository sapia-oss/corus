package org.sapia.corus.db;

import java.io.IOException;

import jdbm.JDBMRecordManager;

import org.sapia.corus.client.services.db.DbMap;


/**
 * Wraps a {@link JDBMRecordManager}.
 * 
 * @author Yanick Duchesne
 */
public class JdbmDb {
  private JDBMRecordManager _recman;

  /**
   * Constructor for JispDb.
   */
  private JdbmDb(JDBMRecordManager recman) {
    _recman = recman;
  }

  <K,V> DbMap<K, V> getDbMap(Class<K> keyType, Class<V> valueType, String name) throws IOException {
    return new DbMapImpl<K, V>(keyType, valueType, _recman.getHashtable(name));
  }

  static JdbmDb open(String fName) throws IOException {
    return new JdbmDb(new JDBMRecordManager(fName));
  }

  void close() {
    try {
      _recman.close();
    } catch (IOException e) {
      // noop;
    }
  }
  
  /*

  public static void main(String[] args) {
    try {
      JdbmDb db = JdbmDb.open("test");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }*/
}
