package org.sapia.corus.database;

import java.util.HashMap;
import java.util.Map;

import org.sapia.corus.client.services.database.Archiver;
import org.sapia.corus.client.services.database.Revision;

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
}
  
  
