package org.sapia.corus.database;

import java.util.Map;
import java.util.Set;

import org.mapdb.DB;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.Revision;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

/**
 * An archiver implemented over Map DB.
 * 
 * @author yduchesne
 *
 */
public class ArchiverImpl<K, V> extends ArchiverSupport<K, V> {
  
  private DB db;
  
  private String prefix;
  
  private Set<String> revSet;
  
  @SuppressWarnings("unchecked")
  public ArchiverImpl(String prefix, DB db) {
    this.prefix = prefix;
    this.db     = db;
    revSet = (Set<String>) db.treeSet(prefix + "-revisions").createOrOpen();
  }
  
  @Override
  protected Map<K, Revision<K, V>> revisions(String revId) {
    revSet.add(revId);
    return revMap(revId);
  }
  
  @Override
  protected Set<RevId> revisionIds() {
    return Collects.convertAsSet(revSet, new Func<RevId, String>() {
      @Override
      public RevId call(String revId) {
        return RevId.valueOf(revId);
      }
    });
  }

  @SuppressWarnings("unchecked")
  private Map<K, Revision<K, V>> revMap(String revId) {
    return (Map<K, Revision<K, V>>) db.treeMap(prefix + "." + revId).createOrOpen();
  }
}
