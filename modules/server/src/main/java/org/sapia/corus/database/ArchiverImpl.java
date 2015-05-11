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
  
  public ArchiverImpl(String prefix, DB db) {
    this.prefix = prefix;
    this.db     = db;
  }
  
  @Override
  protected Map<K, Revision<K, V>> revisions(String revId) {
    db.getTreeSet(prefix + "-revisions").add(revId);
    return db.getTreeMap(prefix + "." + revId);
  }
  
  @Override
  protected Set<RevId> revisionIds() {
    Set<String> revIds = db.getTreeSet(prefix + "-revisions");
    return Collects.convertAsSet(revIds, new Func<RevId, String>() {
      @Override
      public RevId call(String revId) {
        return RevId.valueOf(revId);
      }
    });
  }

}
