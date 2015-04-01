package org.sapia.corus.database;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.client.services.database.Archiver;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.Revision;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

/**
 * Implements an in-memory {@link Archiver}.
 * 
 * @author yduchesne
 *
 */
public class InMemoryArchiver<K, V> extends ArchiverSupport<K, V> {
  
  private Map<String, Map<K, Revision<K, V>>> revisions = new HashMap<>();
  
  public InMemoryArchiver() {
  }
  
  Map<String, Map<K, Revision<K, V>>> getInternalMap() {
    return revisions;
  }
  
  protected Map<K, Revision<K, V>> revisions(String revId) {
    Map<K, Revision<K, V>> records = revisions.get(revId);
    if (records == null) {
      records = new HashMap<K, Revision<K, V>>();
      revisions.put(revId, records);
    }
    return records;
  }
  
  @Override
  protected Set<RevId> revisionIds() {
    return Collects.convertAsSet(revisions.keySet(), new Func<RevId, String>() {
      @Override
      public RevId call(String revId) {
        return RevId.valueOf(revId);
      }
    });
  }
}
  
  
