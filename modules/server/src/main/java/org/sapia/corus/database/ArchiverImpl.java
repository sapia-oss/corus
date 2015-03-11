package org.sapia.corus.database;

import java.util.Map;

import org.mapdb.DB;
import org.sapia.corus.client.services.database.Revision;

/**
 * An archiver implemented over Map DB.
 * 
 * @author yduchesne
 *
 */
public class ArchiverImpl<K, V> extends ArchiverSupport<K, V> {
  
  private DB db;
  
  private String prefix;
  
  public ArchiverImpl(String prefix, DB db) {
    this.prefix = prefix;
    this.db     = db;
  }
  
  @Override
  protected Map<K, Revision<K, V>> revisions(String revId) {
    return db.getTreeMap(prefix + "." + revId) ;
  }

}
